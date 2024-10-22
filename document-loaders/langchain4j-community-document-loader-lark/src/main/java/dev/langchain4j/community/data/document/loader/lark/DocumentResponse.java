package dev.langchain4j.community.data.document.loader.lark;

class DocumentResponse {

    private String content;

    DocumentResponse() {
    }

    DocumentResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
