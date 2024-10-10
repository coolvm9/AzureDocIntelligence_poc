package com.fusion;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

public class DocIntelligence {

    // set `<your-endpoint>` and `<your-key>` variables with the values from the Azure portal
    private static final String endpoint = "";
    private static final String key = "";

    public static void main(String[] args) {

        // create your `DocumentIntelligenceClient` instance and `AzureKeyCredential` variable
        DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

        // sample document
        String modelId = "prebuilt-layout";
        String documentUrl = "https://raw.githubusercontent.com/Azure-Samples/cognitive-services-REST-api-samples/master/curl/form-recognizer/sample-layout.pdf";

      /*  SyncPoller <AnalyzeResultOperation, AnalyzeResultOperation> analyzeLayoutPoller =
                client.beginAnalyzeDocument(modelId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new AnalyzeDocumentRequest().setUrlSource(documentUrl));

        AnalyzeResult analyzeLayoutResult = analyzeLayoutPoller.getFinalResult().getAnalyzeResult();

        // pages
        analyzeLayoutResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                    documentPage.getWidth(),
                    documentPage.getHeight(),
                    documentPage.getUnit());


            // lines
            documentPage.getLines().forEach(documentLine ->
                    System.out.printf("Line '%s' is within a bounding polygon %s.%n",
                            documentLine.getContent(),
                            documentLine.getPolygon()));

            // words
            documentPage.getWords().forEach(documentWord ->
                    System.out.printf("Word '%s' has a confidence score of %.2f.%n",
                            documentWord.getContent(),
                            documentWord.getConfidence()));

            // selection marks
            documentPage.getSelectionMarks().forEach(documentSelectionMark ->
                    System.out.printf("Selection mark is '%s' and is within a bounding polygon %s with confidence %.2f.%n",
                            documentSelectionMark.getState().toString(),
                            documentSelectionMark.getPolygon(),
                            documentSelectionMark.getConfidence()));
        });

        // tables
        List < DocumentTable > tables = analyzeLayoutResult.getTables();
        for (int i = 0; i < tables.size(); i++) {
            DocumentTable documentTable = tables.get(i);
            System.out.printf("Table %d has %d rows and %d columns.%n", i, documentTable.getRowCount(),
                    documentTable.getColumnCount());
            documentTable.getCells().forEach(documentTableCell -> {
                System.out.printf("Cell '%s', has row index %d and column index %d.%n", documentTableCell.getContent(),
                        documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
            });
            System.out.println();
        }*/

        // styles
//        analyzeLayoutResult.getStyles().forEach(documentStyle -
//                > System.out.printf("Document is handwritten %s.%n", documentStyle.isHandwritten()));
    }
}