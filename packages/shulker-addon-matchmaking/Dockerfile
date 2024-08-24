FROM --platform=$BUILDPLATFORM node:22 AS builder
WORKDIR /build

ARG BUILDPLATFORM
ARG TARGETPLATFORM

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"

RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --default-toolchain stable
ENV PATH="/root/.cargo/bin:${PATH}"

RUN corepack enable \
  && case "$TARGETPLATFORM" in \
    "linux/amd64") \
      echo "x86_64-unknown-linux-gnu" > /.rust-triple; \
      echo "gcc-x86-64-linux-gnu" > /.rust-compiler \
      ;; \
    "linux/arm64") \
      echo "aarch64-unknown-linux-gnu" > /.rust-triple; \
      echo "gcc-aarch64-linux-gnu" > /.rust-compiler \
      ;; \
    *) exit 1 ;; \
  esac \
  && rustup target add "$(cat /.rust-triple)" \
  && if [ "${BUILDPLATFORM}" != "${TARGETPLATFORM}" ]; then \
    apt-get update \
    && apt-get install -y "$(cat /.rust-compiler)" \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*; \
  fi

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile

COPY . .

RUN pnpm exec nx build shulker-addon-matchmaking -- --target=$(cat /.rust-triple) \
  && cp dist/rust/$(cat /.rust-triple)/release/shulker-addon-matchmaking-director dist/rust/release/shulker-addon-matchmaking-director \
  && cp dist/rust/$(cat /.rust-triple)/release/shulker-addon-matchmaking-mmf dist/rust/release/shulker-addon-matchmaking-mmf

FROM gcr.io/distroless/cc-debian12:nonroot
COPY --from=builder /build/dist/rust/release/shulker-addon-matchmaking-director /
COPY --from=builder /build/dist/rust/release/shulker-addon-matchmaking-mmf /
ENTRYPOINT [ "/shulker-addon-matchmaking-director" ]
