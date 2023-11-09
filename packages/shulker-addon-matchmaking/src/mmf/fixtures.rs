use std::{pin::Pin, sync::Arc};

use futures::{Future, Stream};
use google_open_match_sdk::{
    query_service_client::QueryServiceClient,
    query_service_server::{QueryService, QueryServiceServer},
    Backfill, MatchProfile, QueryBackfillsRequest, QueryBackfillsResponse, QueryTicketIdsRequest,
    QueryTicketIdsResponse, QueryTicketsRequest, QueryTicketsResponse, Ticket,
};
use tempfile::NamedTempFile;
use tokio::{
    net::{UnixListener, UnixStream},
    sync::mpsc,
};
use tokio_stream::wrappers::{ReceiverStream, UnixListenerStream};
use tonic::{
    transport::{Channel, Endpoint, Server, Uri},
    Response, Status,
};
use tower::service_fn;
use uuid::Uuid;

use crate::extensions::{set_max_players_in_profile, set_min_players_in_profile};

pub enum Scenario {
    NoTicketsNoBackfills,
    TicketsWithNoExistingBackfills(Vec<Ticket>),
    TicketsWithExistingBackfills(Vec<Ticket>, Vec<Backfill>),
}

struct QueryServiceStub {
    scenario: Scenario,
}

impl QueryServiceStub {
    fn from_scenario(scenario: Scenario) -> Self {
        QueryServiceStub { scenario }
    }
}

#[tonic::async_trait]
impl QueryService for QueryServiceStub {
    type QueryTicketsStream =
        Pin<Box<dyn Stream<Item = Result<QueryTicketsResponse, Status>> + Send>>;

    async fn query_tickets(
        &self,
        _request: tonic::Request<QueryTicketsRequest>,
    ) -> std::result::Result<tonic::Response<Self::QueryTicketsStream>, tonic::Status> {
        let tickets = match &self.scenario {
            Scenario::TicketsWithNoExistingBackfills(tickets) => tickets.clone(),
            Scenario::TicketsWithExistingBackfills(tickets, _) => tickets.clone(),
            _ => vec![],
        };

        let (tx, rx) = mpsc::channel(128);

        tokio::spawn(async move {
            let res = QueryTicketsResponse { tickets };
            tx.send(Result::<_, Status>::Ok(res)).await.unwrap();
        });

        let stream = ReceiverStream::new(rx);
        Ok(Response::new(Box::pin(stream)))
    }

    type QueryTicketIdsStream =
        Pin<Box<dyn Stream<Item = Result<QueryTicketIdsResponse, Status>> + Send>>;

    async fn query_ticket_ids(
        &self,
        _request: tonic::Request<QueryTicketIdsRequest>,
    ) -> std::result::Result<tonic::Response<Self::QueryTicketIdsStream>, tonic::Status> {
        let tickets = match &self.scenario {
            Scenario::TicketsWithNoExistingBackfills(tickets) => tickets.clone(),
            Scenario::TicketsWithExistingBackfills(tickets, _) => tickets.clone(),
            _ => vec![],
        };

        let (tx, rx) = mpsc::channel(128);

        tokio::spawn(async move {
            let res = QueryTicketIdsResponse {
                ids: tickets.iter().map(|t| t.id.clone()).collect(),
            };
            tx.send(Result::<_, Status>::Ok(res)).await.unwrap();
        });

        let stream = ReceiverStream::new(rx);
        Ok(Response::new(Box::pin(stream)))
    }

    type QueryBackfillsStream =
        Pin<Box<dyn Stream<Item = Result<QueryBackfillsResponse, Status>> + Send>>;

    async fn query_backfills(
        &self,
        _request: tonic::Request<QueryBackfillsRequest>,
    ) -> std::result::Result<tonic::Response<Self::QueryBackfillsStream>, tonic::Status> {
        let backfills = match &self.scenario {
            Scenario::TicketsWithExistingBackfills(_, backfills) => backfills.clone(),
            _ => vec![],
        };

        let (tx, rx) = mpsc::channel(128);

        tokio::spawn(async move {
            let res = QueryBackfillsResponse { backfills };
            tx.send(Result::<_, Status>::Ok(res)).await.unwrap();
        });

        let stream = ReceiverStream::new(rx);
        Ok(Response::new(Box::pin(stream)))
    }
}

pub async fn create_query_service_stub(
    scenario: Scenario,
) -> (impl Future<Output = ()>, QueryServiceClient<Channel>) {
    let socket = NamedTempFile::new().unwrap();
    let socket = Arc::new(socket.into_temp_path());
    std::fs::remove_file(&*socket).unwrap();

    let uds = UnixListener::bind(&*socket).unwrap();
    let stream = UnixListenerStream::new(uds);

    let server_fut = async {
        let result = Server::builder()
            .add_service(QueryServiceServer::new(QueryServiceStub::from_scenario(
                scenario,
            )))
            .serve_with_incoming(stream)
            .await;
        assert!(result.is_ok());
    };

    let socket = Arc::clone(&socket);
    let channel = Endpoint::try_from("http://any.url")
        .unwrap()
        .connect_with_connector(service_fn(move |_: Uri| {
            let socket = Arc::clone(&socket);
            async move { UnixStream::connect(&*socket).await }
        }))
        .await
        .unwrap();

    let client = QueryServiceClient::new(channel);
    (server_fut, client)
}

pub fn create_random_profile(min_players: Option<i32>, max_players: i32) -> MatchProfile {
    let mut profile = MatchProfile {
        name: Uuid::new_v4().to_string(),
        ..MatchProfile::default()
    };
    if let Some(min_players) = min_players {
        set_min_players_in_profile(&mut profile, min_players);
    }
    set_max_players_in_profile(&mut profile, max_players);

    profile
}

pub fn create_random_ticket() -> Ticket {
    Ticket {
        id: Uuid::new_v4().to_string(),
        ..Ticket::default()
    }
}
