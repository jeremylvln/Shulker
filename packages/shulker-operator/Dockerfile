FROM node:20 AS builder
WORKDIR /build

RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --default-toolchain stable

ENV PATH="/root/.cargo/bin:${PATH}"

COPY package.json package-lock.json ./
RUN npm i --ignore-scripts

COPY . .
RUN npm run prepare

RUN npx nx build shulker-operator

FROM gcr.io/distroless/cc-debian12:nonroot
COPY --from=builder /build/dist/rust/release/shulker-operator /
ENTRYPOINT [ "/shulker-operator" ]
