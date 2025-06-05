#ifndef TAGLIB_GLUE
#define TAGLIB_GLUE

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    void* fileRef;
} TaglibFile;

/**
List of usertexts.
Keys and values share the same index in their respective arrays.
A value may be null.
*/
typedef struct {
    unsigned int size;
    const char** keys;
    const char** values;
} TaglibUsertextList;

/**
    List of chapters.
    All field-items share the same index in their respective arrays.
    Title may be null if non could be found for the chapter or the chapter could not be parsed.
    Times are in milliseconds.
*/
typedef struct {
    unsigned int size;
    const char** titles;
    unsigned int* startTimes;
    unsigned int* endTimes;
} TaglibChapterList;

typedef enum {
    UNDETERMINED,
    AUDIO,
    VIDEO
} FileType;

/**
Returns the opened file or null on error.
Must be freed with close_file().
*/
TaglibFile* taglib_open_file(const char* path);

void taglib_close_file(TaglibFile* file);

/**
Returns a C-string with the title or null if there is none.
Must be freed with free_str().
*/
const char* taglib_get_title(TaglibFile* file);

/**
Returns a C-string with the artist or null if there is none.
Must be freed with free_str().
*/
const char* taglib_get_artist(TaglibFile* file);

/**
Returns a C-string with the genre or null if there is none.
Must be freed with free_str().
*/
const char* taglib_get_genre(TaglibFile* file);

/**
Returns a list of usertexts or null if there are none.
Must be freed with taglib_free_usertext_list().
*/
const TaglibUsertextList* taglib_get_usertexts(TaglibFile* file);

/**
Returns a list of chapters or null if there are none.
Must be freed with taglib_free_chapter_list().
*/
const TaglibChapterList* taglib_get_chapters(TaglibFile* file);

/**
Returns the length of the file in milliseconds.
Will return -1 on error;
*/
int taglib_get_length(TaglibFile* file);

FileType taglib_get_filetype(TaglibFile* file);

void taglib_free_str(const void* str);

void taglib_free_usertext_list(const TaglibUsertextList* list);

void taglib_free_chapter_list(const TaglibChapterList*);

#ifdef __cplusplus
}
#endif

#endif
