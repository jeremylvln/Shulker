#!/bin/bash

ln -s cmd/shulker-operator/main.go main.go
kubebuilder $@
rm main.go
