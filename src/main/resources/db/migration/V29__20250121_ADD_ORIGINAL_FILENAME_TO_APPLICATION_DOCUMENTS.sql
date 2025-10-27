-- V29__20250121_ADD_ORIGINAL_FILENAME_TO_APPLICATION_DOCUMENTS.sql
-- Add original_filename column to application_documents table
-- This column stores the original filename from user upload for better document tracking

ALTER TABLE application_documents
ADD COLUMN original_filename VARCHAR(255);

-- Add comment for documentation
COMMENT ON COLUMN application_documents.original_filename IS 'Original filename from user upload for document tracking';
