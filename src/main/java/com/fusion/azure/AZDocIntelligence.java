package com.fusion.azure;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AZDocIntelligence {

    public static void main(String[] args) {
        try {
            System.setProperty("AZURE_LOG_LEVEL", "verbose");
            // Define the path to the external properties file
            String externalConfigPath = "/Users/satyaanumolu/POCs/cloudprops/azure.props";

//            String endpoint = System.getenv("AZURE_ENDPOINT");
//            System.out.println(endpoint);
            // Load properties from the external file
            Properties prop = new Properties();
            try (FileInputStream input = new FileInputStream(externalConfigPath)) {
                prop.load(input);
            }

            // Retrieve key and endpoint from properties file
            String endpoint = prop.getProperty("azure.docai.endpoint");
            String key = prop.getProperty("azure.docai.key");
            System.out.println(endpoint);
            System.out.println(key);
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Key: " + key);


            // Instantiate the client with credentials
            DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
                    .credential(new AzureKeyCredential(key))
                    .endpoint(endpoint)
                    .buildClient();



            File file = new File(DocumentIntelligenceClient.class.getClassLoader().getResource("sample-layout.pdf").getFile());
            Path filePath = file.toPath();
            System.out.println("File Path: " + filePath.toString());

            BinaryData layoutDocumentData = BinaryData.fromFile(filePath, (int) file.length());


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
            AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();
            // Print JSON output
            System.out.println("JSON Output:");
//            analyzeLayoutResult.toJson(System.out);

            printGeometricAndLogicalRoles(analyzeLayoutResult);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printGeometricAndLogicalRoles(AnalyzeResult analyzeLayoutResult) {
        System.out.println("Processing pages for geometric and logical roles...");

        analyzeLayoutResult.getParagraphs().forEach(paragraph -> {
            System.out.println("Paragraph Content: " + paragraph.getContent());
            System.out.println("Logical Role: " + (paragraph.getRole() != null ? paragraph.getRole() : "No role assigned"));
            System.out.println("Bounding Box: " + paragraph.getBoundingRegions().toString());
        });

        // Iterate through each page
        analyzeLayoutResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                    documentPage.getWidth(),
                    documentPage.getHeight(),
                    documentPage.getUnit());

            // Iterate through each line
            documentPage.getLines().forEach(documentLine ->
                    System.out.printf("Line '%s' is within a bounding polygon %s.%n",
                            documentLine.getContent(),
                            documentLine.getPolygon()));

            // Iterate through each word
            documentPage.getWords().forEach(documentWord ->
                    System.out.printf("Word '%s' has a confidence score of %.2f.%n",
                            documentWord.getContent(),
                            documentWord.getConfidence()));

            // Iterate through each selection mark
            documentPage.getSelectionMarks().forEach(documentSelectionMark ->
                    System.out.printf("Selection mark is '%s' and is within a bounding polygon %s with confidence %.2f.%n",
                            documentSelectionMark.getState().toString(),
                            documentSelectionMark.getPolygon(),
                            documentSelectionMark.getConfidence()));
        });
    }
}
