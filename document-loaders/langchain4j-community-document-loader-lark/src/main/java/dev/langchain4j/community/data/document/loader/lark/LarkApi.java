package dev.langchain4j.community.data.document.loader.lark;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface LarkApi {

    int OK = 0;

    @GET("open-apis/docx/v1/documents/{documentId}")
    Call<ResponseWrapper<MetadataResponse>> getDocumentMetadata(@Path("documentId") String documentId);

    @GET("open-apis/docx/v1/documents/{documentId}/raw_content")
    Call<ResponseWrapper<DocumentResponse>> getDocument(@Path("documentId") String documentId);
}
