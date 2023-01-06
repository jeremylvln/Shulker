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
COPY packages/shulker-operator packages/shulker-operator

RUN cd packages/shulker-operator/src && \
  CGO_ENABLED=0 GOOS=${TARGETOS:-linux} GOARCH=${TARGETARCH} go build -a -o /build/shulker-operator .

FROM gcr.io/distroless/static:nonroot
WORKDIR /

COPY --from=builder /build/shulker-operator .
USER 65532:65532

ENTRYPOINT ["/shulker-operator"]
