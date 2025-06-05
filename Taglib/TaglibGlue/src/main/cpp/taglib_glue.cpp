#include "taglib_glue.h"
#include <string.h>
#include "fileref.h"
#include "tag.h"
#include "id3v2tag.h"
#include "chapterframe.h"
#include "textidentificationframe.h"
#include "mpegfile.h"
#include "apefile.h"
#include "asffile.h"
#include "dsffile.h"
#include "flacfile.h"
#include "mp4file.h"
#include "mpcfile.h"
#include "oggfile.h"
#include "rifffile.h"
#include "shortenfile.h"
#include "trueaudiofile.h"
#include "wavpackfile.h"
#include "itfile.h"
#include "modfile.h"
#include "s3mfile.h"
#include "xmfile.h"

using TagLib::FileRef;
using TagLib::String;
using TagLib::StringList;
using TagLib::List;
using TagLib::AudioProperties;

static void freeCStr(const char* str) {
    free(const_cast<char*>(str));
}

static const char* taglibStrToCStr(String str) {
    const std::string stdStr = str.to8Bit(true);
    return ::strdup(stdStr.c_str());
}

TaglibFile* taglib_open_file(const char* path) {
    try {
        FileRef* f = new FileRef(path, true, AudioProperties::Average);
        if(f->isNull()) {
            delete f;
            return nullptr;
        }

        TaglibFile* ret = new TaglibFile;
        ret->fileRef = reinterpret_cast<void*>(f);
        return ret;
    } catch(...) {
        return nullptr;
    }
}

void taglib_close_file(TaglibFile* file) {
    delete reinterpret_cast<FileRef*>(file->fileRef);
    delete file;
}

const char* taglib_get_title(TaglibFile* file) {
    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    String val = f->tag()->title();
    if(val.isEmpty())
        return nullptr;
    return taglibStrToCStr(val);
}

const char* taglib_get_artist(TaglibFile* file) {
    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    String val = f->tag()->artist();
    if(val.isEmpty())
        return nullptr;
    return taglibStrToCStr(val);
}

const char* taglib_get_genre(TaglibFile* file) {
    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    String val = f->tag()->genre();
    if(val.isEmpty())
        return nullptr;
    return taglibStrToCStr(val);
}

const TaglibUsertextList* taglib_get_usertexts(TaglibFile* file) {
    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    auto mpegFile = dynamic_cast<TagLib::MPEG::File*>(f->file());
    if(mpegFile == nullptr || !mpegFile->hasID3v2Tag())
        return nullptr;

    const TagLib::ID3v2::Tag* tag = mpegFile->ID3v2Tag(false);
    const TagLib::ID3v2::FrameList& userTexts = tag->frameList("TXXX");
    unsigned int itemCount = userTexts.size();
    if(itemCount == 0)
        return nullptr;

    auto list = new TaglibUsertextList;
    list->size = itemCount;
    list->keys = new const char*[itemCount];
    list->values = new const char*[itemCount];

    for(unsigned int i = 0; i < itemCount; i++) {
        auto userText = dynamic_cast<const TagLib::ID3v2::UserTextIdentificationFrame*>(userTexts[i]);

        list->keys[i] = taglibStrToCStr(userText->description());

        StringList values = userText->fieldList();
        if(values.size() > 1) {// values[0] is the description
            list->values[i] = taglibStrToCStr(values[1]);
        } else {
            list->values[i] = 0;
        }
    }

    return list;
}

const TaglibChapterList* taglib_get_chapters(TaglibFile* file) {
    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    auto mpegFile = dynamic_cast<TagLib::MPEG::File*>(f->file());
    if(mpegFile == nullptr || !mpegFile->hasID3v2Tag())
        return nullptr;

    const TagLib::ID3v2::Tag* tag = mpegFile->ID3v2Tag(false);
    const TagLib::ID3v2::FrameList& chapterFrames = tag->frameList("CHAP");
    unsigned int chapterCount = chapterFrames.size();
    if(chapterCount == 0)
        return nullptr;

    auto list = new TaglibChapterList;
    list->size = chapterCount;
    list->titles = new const char*[chapterCount];
    list->startTimes = new unsigned int[chapterCount];
    list->endTimes = new unsigned int[chapterCount];

    for(unsigned int i = 0; i < chapterCount; i++) {
        auto chapterFrame = dynamic_cast<const TagLib::ID3v2::ChapterFrame*>(chapterFrames[i]);

        unsigned int startTime = chapterFrame->startTime();
        unsigned int endTime = chapterFrame->endTime();

        // offset instead of time is not supported
        if((startTime == 0 && chapterFrame->startOffset() != 0xFFFFFFFF)
            || (endTime == 0 && chapterFrame->endOffset() != 0xFFFFFFFF)) {
            list->startTimes[i] = 0xFFFFFFFF;
            list->endTimes[i] = 0xFFFFFFFF;
            list->titles[i] = 0;
            continue;
        }

        // search title (the first valid text frame)
        const char* title = nullptr;
        const List<TagLib::ID3v2::Frame*>* titleCandidates = &chapterFrame->embeddedFrameList("TIT2");
        if(titleCandidates->isEmpty())
            titleCandidates = &chapterFrame->embeddedFrameList("TIT3");
        if(!titleCandidates->isEmpty()) {
            auto textFrame = dynamic_cast<const TagLib::ID3v2::TextIdentificationFrame*>((*titleCandidates)[0]);
            title = taglibStrToCStr(textFrame->toString());
        }

        list->titles[i] = title;
        list->startTimes[i] = startTime;
        list->endTimes[i] = endTime;
    }

    return list;
}

int taglib_get_length(TaglibFile* file) {
    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    TagLib::AudioProperties* ap = f->audioProperties();
    if(ap == nullptr)
        return -1;
    return ap->lengthInMilliseconds();
}

FileType taglib_get_filetype(TaglibFile* file) {
    // this is a rather crude implementation based on the detected filetype

    FileRef* f = reinterpret_cast<FileRef*>(file->fileRef);
    TagLib::File* tlFile = f->file();

    if(
        dynamic_cast<TagLib::APE::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::ASF::File*>(tlFile) != nullptr// ASF can be audio or video; lets be conservative
        || dynamic_cast<TagLib::DSF::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::FLAC::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::MPC::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::MPEG::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::Ogg::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::RIFF::File*>(tlFile) != nullptr// RIFF can be audio or video; lets be conservative
        || dynamic_cast<TagLib::Shorten::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::TrueAudio::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::WavPack::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::IT::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::S3M::File*>(tlFile) != nullptr
        || dynamic_cast<TagLib::XM::File*>(tlFile) != nullptr
    ) return FileType::AUDIO;

    if(
        dynamic_cast<TagLib::MP4::File*>(tlFile) != nullptr
    ) return FileType::VIDEO;

    return FileType::UNDETERMINED;
}

void taglib_free_str(const void* str) {
    free(const_cast<void*>(str));
}

void taglib_free_usertext_list(const TaglibUsertextList* list) {
    for(unsigned int i = 0; i < list->size; i++) {
        freeCStr(list->keys[i]);
        freeCStr(list->values[i]);
    }

    delete[] list->keys;
    delete[] list->values;
    delete list;
}

void taglib_free_chapter_list(const TaglibChapterList* list) {
    for(unsigned int i = 0; i < list->size; i++) {
        freeCStr(list->titles[i]);
    }

    delete[] list->titles;
    delete[] list->startTimes;
    delete[] list->endTimes;
    delete list;
}
