#ifndef SORTING_H
#define SORTING_H

// Sort integer array
// order: 1 for ascending, 0 for descending
// Returns 1 on success, 0 on error
int sort_array(int* vec, int length, char order);

// Calculate array median
// Returns 1 on success, 0 on error
int median(int* vec, int length, int* result);

#endif