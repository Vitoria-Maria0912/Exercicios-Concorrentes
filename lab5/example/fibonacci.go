package main

import ( "fmt" )

func main() {

	go status()

	resul := fib(50)

	fmt.Println(resul)
}

func fib(n int) int {
	
	if n <= 1 { return n }
	return fib(n - 1) + fib(n - 2)
}

func status() {

	for { fmt.Println("Calma, Jamilly!") }
}