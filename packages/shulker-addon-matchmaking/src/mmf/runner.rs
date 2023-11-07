use futures::StreamExt;
use google_open_match_sdk::{
    query_service_client::QueryServiceClient, Backfill, Match, MatchProfile, Pool,
    QueryBackfillsRequest, QueryTicketsRequest, RunResponse, Ticket,
};
use tokio::sync::mpsc;
use tokio_stream::wrappers::ReceiverStream;
use tonic::{transport::Channel, Status};

pub trait MatchSupplier {
    fn create_matches(
        &self,
        profile: &MatchProfile,
        pool: &Pool,
        tickets: Vec<Ticket>,
        backfills: Vec<Backfill>,
    ) -> Vec<Match>;
}

macro_rules! create_match_function_runner {
    ($typ:ty) => {
        #[tonic::async_trait]
        impl MatchFunction for $typ {
            type RunStream = Pin<Box<dyn Stream<Item = Result<RunResponse, Status>> + Send>>;

            async fn run(
                &self,
                request: tonic::Request<RunRequest>,
            ) -> std::result::Result<tonic::Response<Self::RunStream>, tonic::Status> {
                let profile = request.into_inner().profile.unwrap();

                debug!(profile_name = profile.name, "running mmf for profile");

                let mut proposals = vec![];

                for pool in profile.pools.iter() {
                    let tickets = crate::mmf::runner::query_pool_tickets(
                        self.query_client.clone(),
                        pool.clone(),
                    )
                    .await
                    .map_err(|e| {
                        tonic::Status::internal(format!("Failed to query tickets: {}", e))
                    })?;
                    debug!(
                        profile_name = profile.name,
                        pool_name = pool.name,
                        tickets_count = tickets.len(),
                        "retrieved tickets for pool"
                    );

                    let backfills = crate::mmf::runner::query_pool_backfill_tickets(
                        self.query_client.clone(),
                        pool.clone(),
                    )
                    .await
                    .map_err(|e| {
                        tonic::Status::internal(format!("Failed to query backfills: {}", e))
                    })?;
                    debug!(
                        profile_name = profile.name,
                        pool_name = pool.name,
                        backfills_count = backfills.len(),
                        "retrieved backfills for pool"
                    );

                    let mut matches = self.create_matches(&profile, &pool, tickets, backfills);
                    debug!(
                        profile_name = profile.name,
                        pool_name = pool.name,
                        proposals_count = matches.len(),
                        "created match proposals for pool"
                    );

                    proposals.append(&mut matches);
                }

                debug!(
                    profile_name = profile.name,
                    proposals_count = proposals.len(),
                    "replying match proposals"
                );
                let response_stream = crate::mmf::runner::create_proposal_reply_stream(proposals);
                Ok(Response::new(Box::pin(response_stream) as Self::RunStream))
            }
        }
    };
}

pub(crate) use create_match_function_runner;

pub(crate) async fn query_pool_tickets(
    mut query_client: QueryServiceClient<Channel>,
    pool: Pool,
) -> Result<Vec<Ticket>, anyhow::Error> {
    let mut stream = query_client
        .query_tickets(QueryTicketsRequest { pool: Some(pool) })
        .await?
        .into_inner();

    let mut tickets = vec![];

    while let Some(res) = stream.next().await {
        tickets.append(res.unwrap().tickets.as_mut())
    }

    Ok(tickets)
}

pub(crate) async fn query_pool_backfill_tickets(
    mut query_client: QueryServiceClient<Channel>,
    pool: Pool,
) -> Result<Vec<Backfill>, anyhow::Error> {
    let mut stream = query_client
        .query_backfills(QueryBackfillsRequest { pool: Some(pool) })
        .await?
        .into_inner();

    let mut backfills = vec![];

    while let Some(res) = stream.next().await {
        backfills.append(res.unwrap().backfills.as_mut())
    }

    Ok(backfills)
}

pub(crate) fn create_proposal_reply_stream(
    matches: Vec<Match>,
) -> ReceiverStream<Result<RunResponse, Status>> {
    let (tx, rx) = mpsc::channel(128);

    tokio::spawn(async move {
        for match_proposal in matches.iter() {
            let res = RunResponse {
                proposal: Some(match_proposal.clone()),
            };

            match tx.send(Result::<_, Status>::Ok(res)).await {
                Ok(_) => (),
                Err(_item) => break,
            }
        }
    });

    ReceiverStream::new(rx)
}
