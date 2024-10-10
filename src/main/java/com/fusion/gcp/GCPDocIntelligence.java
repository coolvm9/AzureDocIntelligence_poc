package com.fusion.gcp;

import com.google.cloud.documentai.v1.*;
import com.google.cloud.documentai.v1.Document.Page;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;

public class GCPDocIntelligence {

    public static void main(String[] args) {
        try {
            String projectId = "your-project-id";  // Replace with your GCP Project ID
            String location = "your-processor-location";  // Replace with your processor's location (e.g., us or eu)
            String processorId = "your-processor-id";  // Replace with your processor ID (found in the Google Cloud console)

            // Load the document file
            File file = new File("path/to/sample-layout.pdf");  // Replace with your PDF file path
            Path filePath = file.toPath();
            ByteString content = ByteString.readFrom(new FileInputStream(file));

            // Instantiate the client
            try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {

                // Create the processor name from the project, location, and processor ID
                ProcessorName name = ProcessorName.of(projectId, location, processorId);

                // Create the RawDocument request
                RawDocument rawDocument = RawDocument.newBuilder()
                        .setContent(content)
                        .setMimeType("application/pdf")  // Specify the file type
                        .build();

                // Create the process request
                ProcessRequest request = ProcessRequest.newBuilder()
                        .setName(name.toString())
                        .setRawDocument(rawDocument)
                        .build();

                // Process the document
                ProcessResponse response = client.processDocument(request);
                Document document = response.getDocument();

                // Print JSON output of the processed document
                System.out.println("Document processing complete.");
                System.out.println(document.toString());

                // Print geometric and logical roles
                printGeometricAndLogicalRoles(document);

                // Check for human review status
                HumanReviewStatus reviewStatus = response.getHumanReviewStatus();
                System.out.println("Human Review Status: " + reviewStatus.getState());

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printGeometricAndLogicalRoles(Document document) {
        System.out.println("Processing pages for geometric and logical roles...");

        // Iterate through each page in the document
        List<Page> pages = document.getPagesList();
        for (Page page : pages) {
            System.out.println("\nPage Number: " + page.getPageNumber());

            // Geometric roles: Text, Tables, Figures
            System.out.println("Geometric Roles:");

            // Iterate through text lines
            page.getLinesList().forEach(line -> {
                System.out.println("Geometric Role: Text");
                System.out.println("Line Bounding Box: " + line.getLayout().getBoundingPoly());
                System.out.println("Line Content: " + line.getLayout().getTextAnchor().getContent());
            });

            // Iterate through tables
            System.out.println("Tables:");
            page.getTablesList().forEach(table -> {
                System.out.println("Geometric Role: Table");
                System.out.println("Table with " + table.getBodyRowsList().size() + " rows and " + table.getBodyRowsList().size() + " columns.");
                table.getBodyRowsList().forEach(row -> row.getCellsList().forEach(cell ->
                        System.out.println("Cell Content: " + cell.getLayout().getTextAnchor().getContent())));
            });

            // Logical roles: Titles, Headings, Footers
            System.out.println("Logical Roles:");
            page.getParagraphsList().forEach(paragraph -> {
                System.out.println("Paragraph Content: " + paragraph.getLayout().getTextAnchor().getContent());
                System.out.println("Bounding Box: " + paragraph.getLayout().getBoundingPoly());
            });

            System.out.println("------------------------------------------------");
        }
    }
}