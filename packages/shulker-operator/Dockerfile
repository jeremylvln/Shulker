FROM golang:1.19 as builder
ARG TARGETOS
ARG TARGETARCH

WORKDIR /build
COPY go.mod go.mod
COPY go.sum go.sum
RUN go mod download

COPY packages/crds packages/crds
COPY packages/controller-utils packages/controller-utils
COPY packages/resource-utils packages/resource-utils
COPY packages/shulker-operator packages/shulker-operator

RUN cd packages/shulker-operator/src && \
  CGO_ENABLED=0 GOOS=${TARGETOS:-linux} GOARCH=${TARGETARCH} go build -a -o /build/shulker-operator .

FROM gcr.io/distroless/static:nonroot
WORKDIR /

COPY --from=builder /build/shulker-operator .
USER 65532:65532

ENTRYPOINT ["/shulker-operator"]
