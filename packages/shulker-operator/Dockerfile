FROM rust:1.72 AS builder
WORKDIR /build

COPY . .
RUN cargo build --release --bin shulker-operator

FROM gcr.io/distroless/cc-debian12:nonroot
COPY --from=builder /build/dist/rust/release/shulker-operator /
CMD [ "/shulker-operator" ]
