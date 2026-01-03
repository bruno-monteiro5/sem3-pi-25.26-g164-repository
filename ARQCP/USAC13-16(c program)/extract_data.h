#ifndef EXTRACT_DATA_H
#define EXTRACT_DATA_H

// Extrai dados do formato TOKEN&unit:xxx&value:yy
// Retorna 1 em sucesso, 0 em erro
int extract_data(char* str, char* token, char* unit, int* value);

#endif