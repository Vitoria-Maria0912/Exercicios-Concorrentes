package main

import (
	"fmt"
	"math/rand"
	"time"
	"unicode"
)

//below random string functions are based on Jon Calhoun code
const charset = "abcdefghijklmnopqrstuvwxyz1234567890"

var seededRand *rand.Rand = rand.New(
	rand.NewSource(time.Now().UnixNano()))

func StringWithCharset(length int, charset string) string {
	b := make([]byte, length)
	for i := range b {
		b[i] = charset[seededRand.Intn(len(charset))]
	}
	return string(b)
}

func RandString(length int) string {
	return StringWithCharset(length, charset)
}

func isLetter(s string) bool {
	for _, r := range s {
		if !unicode.IsLetter(r) {
			return false
		}
	}
	return true
}

func producer(ch chan int, length int) {
	for { go RandString(length) }
}

func consumer(ch chan int) {
	for str := range ch {
		if isLetter(str) { fmt.Printf("Only letters: %s\n", str) }
	}
}

func main() {

	ch := make(chan string)
	producer(ch, 5)
	consumer(ch)

	select{}
}
