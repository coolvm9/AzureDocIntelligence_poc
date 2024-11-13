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
                // Print Page Details
                analyzeLayoutResult.getPages().forEach(page -> {
                    System.out.println("Page Number: " + page.getPageNumber());
                    page.getLines().forEach(line -> {
                        System.out.println("Line: " + line.getContent());
                    });
                });

                // Print Paragraphs
                analyzeLayoutResult.getParagraphs().forEach(paragraph -> {
                    System.out.println("Paragraph: " + paragraph.getContent() + ", Role : " + paragraph.getRole());
                });
                // Print Tables
                analyzeLayoutResult.getTables().forEach(table -> {
                    System.out.println("Table with " + table.getRowCount() + " rows and " + table.getColumnCount() + " columns");
                    table.getCells().forEach(cell -> {
                        System.out.println("Cell Row: " + cell.getRowIndex() +
                                ", Column: " + cell.getColumnIndex() +
                                ", Content: " + cell.getContent());
                    });
                });


            } else {
                System.out.println("Analysis failed or timed out.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}