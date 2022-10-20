package org.sifacai.vlcjellyfin.Bean;

public class MediaAttachments {
    private String Codec;
    private String CodecTag;
    private int Index;
    private String FileName;
    private String MimeType;

    public String getCodec() {
        return Codec;
    }

    public void setCodec(String codec) {
        Codec = codec;
    }

    public String getCodecTag() {
        return CodecTag;
    }

    public void setCodecTag(String codecTag) {
        CodecTag = codecTag;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getMimeType() {
        return MimeType;
    }

    public void setMimeType(String mimeType) {
        MimeType = mimeType;
    }
}
