/*
Copyright 2022.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"time"

	"github.com/Tnze/go-mc/bot"
	"github.com/Tnze/go-mc/chat"
	"github.com/google/uuid"
	"shulkermc.io/m/v2/internal/greetings"
)

type ServerIcon string

type ServerStatus struct {
	Description chat.Message
	Players     struct {
		Max    int
		Online int
		Sample []struct {
			ID   uuid.UUID
			Name string
		}
	}
	Version struct {
		Name     string
		Protocol int
	}
	Favicon ServerIcon
	Delay   time.Duration
}

func main() {
	var serverAddr string
	flag.StringVar(&serverAddr, "server-address", "127.0.0.1:25565", "The address the Minecraft Server listen on.")
	flag.Parse()

	greetings.PrintGreetings("shulker-probe")

	serverStatus, err := getServerStatus(serverAddr)
	if err != nil {
		panic(err)
	}
	fmt.Printf("%v", serverStatus)
}

func getServerStatus(serverAddr string) (*ServerStatus, error) {
	res, delay, err := bot.PingAndList(serverAddr)
	if err != nil {
		return nil, fmt.Errorf("failed to get server status: %v", err)
	}

	var status ServerStatus
	err = json.Unmarshal(res, &status)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal server status: %v", err)
	}

	status.Delay = delay
	return &status, nil
}
