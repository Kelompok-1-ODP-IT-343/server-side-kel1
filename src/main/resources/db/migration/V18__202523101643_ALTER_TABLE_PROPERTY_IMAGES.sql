ALTER TABLE property_images
    ALTER COLUMN file_name SET DEFAULT gen_random_uuid()::text;

ALTER TABLE property_images
    ADD CONSTRAINT property_images_file_name_unique UNIQUE (file_name);
