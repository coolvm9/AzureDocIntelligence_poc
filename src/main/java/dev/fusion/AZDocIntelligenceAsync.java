package dev.fusion;

import com.azure.ai.documentintelligence.DocumentIntelligenceAsyncClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AZDocIntelligenceAsync {

    public static void main(String[] args) {
        try {
            System.setProperty("AZURE_LOG_LEVEL", "verbose");

            // Define the path to the external properties file
            String externalConfigPath = "/Users/satyaanumolu/POCs/cloudprops/azure.props";

            // Load properties from the external file
            Properties prop = new Properties();
            try (FileInputStream input = new FileInputStream(externalConfigPath)) {
                prop.load(input);
            }

            // Retrieve key and endpoint from properties file
            String endpoint = prop.getProperty("azure.docai.endpoint");
            String key = prop.getProperty("azure.docai.key");

            // Instantiate the client with credentials
            DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
                    .credential(new AzureKeyCredential(key))
                    .endpoint(endpoint)
                    .buildClient();
            // Use this https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeLayoutAsync.java

           /* DocumentIntelligenceAsyncClient asyncClient = new DocumentIntelligenceClientBuilder()
                    .credential(new AzureKeyCredential(key))
                    .endpoint(endpoint)
                    .buildAsyncClient();*/

            List<DocumentAnalysisFeature> features = Arrays.asList(
                    DocumentAnalysisFeature.FORMULAS,
                    DocumentAnalysisFeature.KEY_VALUE_PAIRS,
                    DocumentAnalysisFeature.BARCODES
            );
            List<String> queryFields = Arrays.asList(
                    "InvoiceDate",
                    "InvoiceTotal",
                    "VendorName"
            );

            // Load the PDF file
            File file = new File(DocumentIntelligenceClient.class.getClassLoader().getResource("sample-layout.pdf").getFile());
            Path filePath = file.toPath();
            BinaryData layoutDocumentData = BinaryData.fromFile(filePath, (int) file.length());

            // Start the document analysis for prebuilt-layout
            System.out.println("Submitting document for analysis...");

           SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutResultPoller =
                    client.beginAnalyzeDocument("prebuilt-layout",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(file.toPath())));



            // Poll the operation until it is complete
            PollResponse<AnalyzeResultOperation> pollResponse;
            do {
                pollResponse = analyzeLayoutResultPoller.poll();
                System.out.println("Status: " + pollResponse.getStatus());

                // Wait for a few seconds before checking the status again
                Thread.sleep(3000);
            } while (!pollResponse.getStatus().isComplete());

            // Get the final result after the polling completes
            if (pollResponse.getStatus().isComplete()) {
                System.out.println("Analysis completed.");
                AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();
                // Print the result (you can further process it as needed)
                System.out.println("Analyze Result: " + analyzeLayoutResult.toJsonString());

                analyzeLayoutResult.getParagraphs().forEach(documentParagraph ->
                        System.out.printf("Paragraph '%s' is within a bounding box %s.%n and role is %s \n",
                                documentParagraph.getContent(),
                                documentParagraph.getBoundingRegions().toString(), documentParagraph.getRole()));

                analyzeLayoutResult.getTables().forEach(documentTable -> {
                    System.out.printf("Table has %d rows and %d columns.%n", documentTable.getRowCount(),
                            documentTable.getColumnCount());
                    documentTable.getCells().forEach(documentTableCell -> {
                        System.out.printf("Cell '%s', has row index %d and column index %d.%n", documentTableCell.getContent(),
                                documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
                    });
                });
                // pages

                analyzeLayoutResult.getPages().forEach(documentPage -> {
                    System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                            documentPage.getWidth(),
                            documentPage.getHeight(),
                            documentPage.getUnit());

                    // lines
                    documentPage.getLines().forEach(documentLine ->
                            System.out.printf("Line '%s' is within a bounding box %s.%n",
                                    documentLine.getContent(),
                                    documentLine.getPolygon().toString()));


                    // selection marks
                    documentPage.getSelectionMarks().forEach(documentSelectionMark ->
                            System.out.printf("Selection mark is '%s' and is within a bounding box %s with confidence %.2f.%n",
                                    documentSelectionMark.getState().toString(),
                                    documentSelectionMark.getPolygon().toString(),
                                    documentSelectionMark.getConfidence()));
                });

// tables
                List<DocumentTable> tables = analyzeLayoutResult.getTables();
                for (int i = 0; i < tables.size(); i++) {
                    DocumentTable documentTable = tables.get(i);
                    System.out.printf("Table %d has %d rows and %d columns.%n", i, documentTable.getRowCount(),
                            documentTable.getColumnCount());
                    documentTable.getCells().forEach(documentTableCell -> {
                        System.out.printf("Cell '%s', has row index %d and column index %d.%n", documentTableCell.getContent(),
                                documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
                    });
                    System.out.println();
                }


            } else {
                System.out.println("Analysis failed or timed out.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}