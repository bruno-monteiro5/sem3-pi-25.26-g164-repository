#ifndef ENCRYPT_DATA_H
#define ENCRYPT_DATA_H

// Encrypts the given data using a key-using caesar cypher
// Returns 1 on success, 0 on error
int encrypt_data(char* in, int key, char* out);

#endif
