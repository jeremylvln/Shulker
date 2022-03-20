package greetings

import "fmt"

func PrintGreetings(componentName string) {
	fmt.Printf("\n")
	fmt.Printf("       ########\n")
	fmt.Printf("   #####      ##\n")
	fmt.Printf("   ##          ##       %s\n", componentName)
	fmt.Printf("   ###   #########      %s\n", "v0.0.0")
	fmt.Printf("   # #####       #\n")
	fmt.Printf("   #  ##         #\n")
	fmt.Printf("      ##\n")
	fmt.Printf("\n")
}
