Summary of Conditions in the Schema

	1.	Pages and Sections:
	•	The document must include either pages or sections at the top level (one or both required).
	•	Pages can have:
	•	Elements directly if sections are not present.
	•	Sections, but then elements must exist within sections and not directly on the page.
	2.	Elements:
	•	Every element must have:
	•	A unique id.
	•	An element_type (one of: paragraph, table, visual element, others).
	•	content describing the element.
	•	A bounding_box with x, y, width, and height to store spatial coordinates.
	3.	Security:
	•	The security property is optional.
	•	If included, it must define:
	•	readonly: An array of groups or users with read-only access.


Validation Rules

	•	Pages and Sections:
	•	If a page contains sections, it cannot have elements directly.
	•	Bounding Box:
	•	Mandatory for every element to include spatial information (x, y, width, height).
	•	Metadata:
	•	The Document and all nested components (pages, sections) must include required metadata.

Let me know if you need further clarifications!