package main

import (
	"fmt"
	"io"
	"os"
	"path/filepath"
	"sync"
)

type Sum struct {
	totalSum int64
	semaphore chan struct{}
	maps      map[int64]map[string]struct{}
	mutex     sync.Mutex
}

func NewSum(limit int) *Sum {
	return &Sum{
		semaphore: make(chan struct{}, limit),
		maps:      make(map[int64]map[string]struct{}),
	}
}

func (s *Sum) sumFile(filePath string) (int64, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return 0, err
	}
	defer file.Close()

	var sum int64
	buf := make([]byte, 1024)
	for {
		n, err := file.Read(buf)
		if err != nil && err != io.EOF {
			return 0, err
		}
		sum += int64(sumBytes(buf[:n]))
		if err == io.EOF {
			break
		}
	}
	return sum, nil
}

func sumBytes(data []byte) int {
	sum := 0
	for _, b := range data {
		sum += int(b)
	}
	return sum
}

func (s *Sum) processFile(path string, wg *sync.WaitGroup) {
	defer wg.Done()
	s.semaphore <- struct{}{} // Acquire semaphore
	defer func() { <- s.semaphore }() // Release semaphore

	if info, err := os.Stat(path); err != nil || info.IsDir() {
		fmt.Println("Non-regular file:", path)
		return
	}

	sum, err := s.sumFile(path)
	if err != nil {
		fmt.Println("Error processing", path, ":", err)
		return
	}

	fmt.Println(path, ":", sum)

	s.mutex.Lock()
	s.totalSum += sum
	if _, exists := s.maps[sum]; !exists {
		s.maps[sum] = make(map[string]struct{})
	}
	s.maps[sum][path] = struct{}{}
	s.mutex.Unlock()
}

func main() {
	if len(os.Args) < 2 {
		fmt.Println("Usage: go run sum.go filepath1 filepath2 ...")
		os.Exit(1)
	}

	s := NewSum(len(os.Args) / 2)
	var wg sync.WaitGroup

	for _, path := range os.Args[1:] {
		wg.Add(1)
		go s.processFile(path, &wg)
	}

	wg.Wait()

	fmt.Println("Total sum:", s.totalSum)
	for sum, files := range s.maps {
		fmt.Println("Sum:", sum, "Files:", files)
	}
}
