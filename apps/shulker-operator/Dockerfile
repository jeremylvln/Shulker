FROM golang:1.19 as builder
ARG TARGETOS
ARG TARGETARCH

WORKDIR /build
COPY go.mod go.mod
COPY go.sum go.sum
RUN go mod download

COPY libs/crds libs/crds
COPY libs/controllers libs/controllers
COPY libs/resources libs/resources
COPY apps/shulker-operator apps/shulker-operator

RUN CGO_ENABLED=0 GOOS=${TARGETOS:-linux} GOARCH=${TARGETARCH} \
  go build -a -o shulker-operator apps/shulker-operator/src/main.go

FROM gcr.io/distroless/static:nonroot
WORKDIR /

COPY --from=builder /build/shulker-operator .
USER 65532:65532

ENTRYPOINT ["/shulker-operator"]
