#ifndef EXTRACT_DATA_H
#define EXTRACT_DATA_H

// Extract data from format TOKEN&unit:xxx&value:yy
// Returns 1 on success, 0 on error
int extract_data(char* str, char* token, char* unit, int* value);

#endif