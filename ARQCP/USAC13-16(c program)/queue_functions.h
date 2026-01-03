#ifndef QUEUE_FUNCTIONS_H
#define QUEUE_FUNCTIONS_H

// Add value to circular buffer
// Returns 1 if buffer became full, 0 otherwise
int enqueue_value(int* buffer, int length, int* nelem, int* tail, int* head, int value);

// Remove value from circular buffer
// Returns 1 on success, 0 if buffer is empty
int dequeue_value(int* buffer, int length, int* nelem, int* tail, int* head, int* value);

// Move n elements from buffer to array
// Returns 1 on success, 0 if insufficient elements
int move_n_to_array(int* buffer, int length, int* nelem, int* tail, int* head, int n, int* array);

#endif