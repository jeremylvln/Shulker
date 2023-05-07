FROM golang:1.19 as builder
ARG TARGETOS
ARG TARGETARCH

WORKDIR /build
COPY go.mod go.mod
COPY go.sum go.sum
RUN go mod download

COPY packages/shulker-crds packages/shulker-crds
COPY packages/shulker-controller-utils packages/shulker-controller-utils
COPY packages/shulker-resource-utils packages/shulker-resource-utils
COPY packages/shulker-addon-matchmaking packages/shulker-addon-matchmaking

RUN cd packages/shulker-addon-matchmaking/src && \
  CGO_ENABLED=0 GOOS=${TARGETOS:-linux} GOARCH=${TARGETARCH} go build -a -o /build/shulker-addon-matchmaking .

FROM gcr.io/distroless/static:nonroot
WORKDIR /

COPY --from=builder /build/shulker-addon-matchmaking .
USER 65532:65532

ENTRYPOINT ["/shulker-addon-matchmaking"]
