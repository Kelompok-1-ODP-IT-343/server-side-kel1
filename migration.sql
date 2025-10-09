-- Create ENUM types first
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended', 'pending_verification');
CREATE TYPE gender_type AS ENUM ('male', 'female');
CREATE TYPE marital_status_type AS ENUM ('single', 'married', 'divorced', 'widowed');
CREATE TYPE property_type AS ENUM ('rumah', 'apartemen', 'ruko', 'tanah', 'townhouse', 'villa');
CREATE TYPE listing_type AS ENUM ('primary', 'secondary');
CREATE TYPE certificate_type AS ENUM ('shm', 'hgb', 'hgu', 'hp', 'girik', 'petok_d');
CREATE TYPE property_status AS ENUM ('available', 'reserved', 'sold', 'off_market', 'under_construction');
CREATE TYPE specialization_type AS ENUM ('residential', 'commercial', 'mixed', 'industrial');
CREATE TYPE partnership_level AS ENUM ('bronze', 'silver', 'gold', 'platinum');
CREATE TYPE developer_status AS ENUM ('active', 'inactive', 'suspended');
CREATE TYPE image_type AS ENUM ('exterior', 'interior', 'floor_plan', 'site_plan', 'location', 'amenities', '360_view');
CREATE TYPE image_category AS ENUM ('main', 'gallery', 'thumbnail');
CREATE TYPE feature_category AS ENUM ('interior', 'exterior', 'amenities', 'location', 'security', 'utilities');
CREATE TYPE poi_type AS ENUM ('school', 'hospital', 'mall', 'bank', 'mosque', 'church', 'park', 'transport', 'office');
CREATE TYPE inquiry_type AS ENUM ('general', 'viewing', 'pricing', 'kpr', 'negotiation');
CREATE TYPE inquiry_status AS ENUM ('new', 'contacted', 'scheduled', 'completed', 'closed');
CREATE TYPE rate_type AS ENUM ('fixed', 'floating', 'mixed');
CREATE TYPE kpr_property_type AS ENUM ('rumah', 'apartemen', 'ruko', 'all');
CREATE TYPE customer_segment AS ENUM ('employee', 'professional', 'entrepreneur', 'pensioner', 'all');
CREATE TYPE staff_position AS ENUM ('branch_manager', 'property_auditor', 'relationship_manager', 'credit_analyst', 'admin');
CREATE TYPE application_property_type AS ENUM ('rumah', 'apartemen', 'ruko', 'tanah');
CREATE TYPE property_certificate_type AS ENUM ('shm', 'hgb', 'hgu', 'hp');
CREATE TYPE application_purpose AS ENUM ('primary_residence', 'investment', 'business');
CREATE TYPE application_status AS ENUM ('draft', 'submitted', 'under_review', 'approved', 'rejected', 'cancelled');
CREATE TYPE document_type AS ENUM ('ktp', 'npwp', 'kk', 'slip_gaji', 'rekening_koran', 'sertifikat_tanah', 'imb', 'pbb', 'akta_nikah', 'surat_keterangan_kerja');
CREATE TYPE workflow_stage AS ENUM ('document_verification', 'credit_analysis', 'property_appraisal', 'final_approval');
CREATE TYPE workflow_status AS ENUM ('pending', 'in_progress', 'approved', 'rejected', 'escalated', 'skipped');
CREATE TYPE priority_level AS ENUM ('low', 'normal', 'high', 'urgent');
CREATE TYPE loan_status AS ENUM ('active', 'completed', 'defaulted', 'restructured');
CREATE TYPE payment_status AS ENUM ('pending', 'paid', 'overdue', 'partial');
CREATE TYPE transaction_type AS ENUM ('payment', 'late_fee', 'penalty', 'refund', 'adjustment');
CREATE TYPE payment_method AS ENUM ('auto_debit', 'transfer', 'cash', 'check');
CREATE TYPE transaction_status AS ENUM ('pending', 'success', 'failed', 'cancelled');
CREATE TYPE debit_status AS ENUM ('success', 'failed', 'insufficient_funds', 'account_blocked');
CREATE TYPE account_type AS ENUM ('savings', 'checking');
CREATE TYPE verification_method AS ENUM ('micro_deposit', 'bank_statement', 'manual');
CREATE TYPE debit_attempt_status AS ENUM ('success', 'failed', 'insufficient_funds', 'account_blocked', 'bank_error');
CREATE TYPE job_type AS ENUM ('auto_debit', 'payment_reminder', 'overdue_notification', 'interest_calculation', 'report_generation', 'data_cleanup');
CREATE TYPE execution_status AS ENUM ('running', 'completed', 'failed', 'timeout');
CREATE TYPE notification_type AS ENUM ('payment_due', 'payment_success', 'payment_failed', 'overdue_warning', 'application_update', 'system_maintenance');
CREATE TYPE notification_channel AS ENUM ('email', 'sms', 'push', 'in_app');
CREATE TYPE notification_status AS ENUM ('pending', 'sent', 'delivered', 'failed');
CREATE TYPE metric_type AS ENUM ('total_applications', 'approved_loans', 'active_loans', 'overdue_payments', 'collection_rate', 'auto_debit_success_rate');
CREATE TYPE setting_type AS ENUM ('string', 'number', 'boolean', 'json');

CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "username" varchar(50) UNIQUE NOT NULL,
  "email" varchar(100) UNIQUE NOT NULL,
  "phone" varchar(20) UNIQUE NOT NULL,
  "password_hash" varchar(255) NOT NULL,
  "role_id" integer NOT NULL,
  "status" user_status DEFAULT 'pending_verification',
  "email_verified_at" timestamp,
  "phone_verified_at" timestamp,
  "last_login_at" timestamp,
  "failed_login_attempts" integer DEFAULT 0,
  "locked_until" timestamp,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "roles" (
  "id" SERIAL PRIMARY KEY,
  "name" varchar(50) UNIQUE NOT NULL,
  "description" text,
  "permissions" json NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "user_profiles" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer UNIQUE NOT NULL,
  "full_name" varchar(100) NOT NULL,
  "nik" varchar(16) UNIQUE NOT NULL,
  "npwp" varchar(15) UNIQUE,
  "birth_date" date NOT NULL,
  "birth_place" varchar(100) NOT NULL,
  "gender" gender_type NOT NULL,
  "marital_status" marital_status_type NOT NULL,
  "address" text NOT NULL,
  "city" varchar(100) NOT NULL,
  "province" varchar(100) NOT NULL,
  "postal_code" varchar(10) NOT NULL,
  "occupation" varchar(100) NOT NULL,
  "company_name" varchar(100),
  "monthly_income" decimal(15,2) NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "user_sessions" (
  "id" varchar(255) PRIMARY KEY,
  "user_id" integer NOT NULL,
  "ip_address" varchar(45) NOT NULL,
  "user_agent" text NOT NULL,
  "payload" text NOT NULL,
  "last_activity" timestamp NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "properties" (
  "id" SERIAL PRIMARY KEY,
  "property_code" varchar(20) UNIQUE NOT NULL,
  "developer_id" integer NOT NULL,
  "property_type" property_type NOT NULL,
  "listing_type" listing_type NOT NULL,
  "title" varchar(255) NOT NULL,
  "description" text NOT NULL,
  "address" text NOT NULL,
  "city" varchar(100) NOT NULL,
  "province" varchar(100) NOT NULL,
  "postal_code" varchar(10) NOT NULL,
  "district" varchar(100) NOT NULL,
  "village" varchar(100) NOT NULL,
  "latitude" decimal(10,8),
  "longitude" decimal(11,8),
  "land_area" decimal(8,2) NOT NULL,
  "building_area" decimal(8,2) NOT NULL,
  "bedrooms" integer NOT NULL,
  "bathrooms" integer NOT NULL,
  "floors" integer DEFAULT 1,
  "garage" integer DEFAULT 0,
  "year_built" integer,
  "price" decimal(15,2) NOT NULL,
  "price_per_sqm" decimal(10,2) NOT NULL,
  "maintenance_fee" decimal(10,2) DEFAULT 0,
  "certificate_type" certificate_type NOT NULL,
  "certificate_number" varchar(100),
  "certificate_area" decimal(8,2),
  "pbb_value" decimal(15,2),
  "status" property_status DEFAULT 'available',
  "availability_date" date,
  "handover_date" date,
  "is_featured" boolean DEFAULT false,
  "is_kpr_eligible" boolean DEFAULT true,
  "min_down_payment_percent" decimal(5,2) DEFAULT 20,
  "max_loan_term_years" integer DEFAULT 20,
  "slug" varchar(255) UNIQUE NOT NULL,
  "meta_title" varchar(255),
  "meta_description" text,
  "keywords" text,
  "view_count" integer DEFAULT 0,
  "inquiry_count" integer DEFAULT 0,
  "favorite_count" integer DEFAULT 0,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "published_at" timestamp
);

CREATE TABLE "developers" (
  "id" SERIAL PRIMARY KEY,
  "company_name" varchar(255) NOT NULL,
  "company_code" varchar(20) UNIQUE NOT NULL,
  "business_license" varchar(100) NOT NULL,
  "developer_license" varchar(100) NOT NULL,
  "contact_person" varchar(100) NOT NULL,
  "phone" varchar(20) NOT NULL,
  "email" varchar(100) NOT NULL,
  "website" varchar(255),
  "address" text NOT NULL,
  "city" varchar(100) NOT NULL,
  "province" varchar(100) NOT NULL,
  "postal_code" varchar(10) NOT NULL,
  "established_year" integer,
  "description" text,
  "specialization" specialization_type,
  "is_partner" boolean DEFAULT false,
  "partnership_level" partnership_level,
  "commission_rate" decimal(5,4) DEFAULT 0.025,
  "status" developer_status DEFAULT 'active',
  "verified_at" timestamp,
  "verified_by" integer,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "property_images" (
  "id" SERIAL PRIMARY KEY,
  "property_id" integer NOT NULL,
  "image_type" image_type NOT NULL,
  "image_category" image_category NOT NULL,
  "file_name" varchar(255) NOT NULL,
  "file_path" varchar(500) NOT NULL,
  "file_size" integer NOT NULL,
  "mime_type" varchar(100) NOT NULL,
  "width" integer,
  "height" integer,
  "alt_text" varchar(255),
  "caption" text,
  "sort_order" integer DEFAULT 0,
  "is_primary" boolean DEFAULT false,
  "uploaded_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "property_features" (
  "id" SERIAL PRIMARY KEY,
  "property_id" integer NOT NULL,
  "feature_category" feature_category NOT NULL,
  "feature_name" varchar(100) NOT NULL,
  "feature_value" varchar(255),
  "is_highlight" boolean DEFAULT false,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "property_locations" (
  "id" SERIAL PRIMARY KEY,
  "property_id" integer NOT NULL,
  "poi_type" poi_type NOT NULL,
  "poi_name" varchar(255) NOT NULL,
  "distance_km" decimal(5,2) NOT NULL,
  "travel_time_minutes" integer,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "property_favorites" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer NOT NULL,
  "property_id" integer NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "property_inquiries" (
  "id" SERIAL PRIMARY KEY,
  "property_id" integer NOT NULL,
  "user_id" integer,
  "name" varchar(100) NOT NULL,
  "email" varchar(100) NOT NULL,
  "phone" varchar(20) NOT NULL,
  "message" text NOT NULL,
  "inquiry_type" inquiry_type NOT NULL,
  "status" inquiry_status DEFAULT 'new',
  "assigned_to" integer,
  "responded_at" timestamp,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "kpr_rates" (
  "id" SERIAL PRIMARY KEY,
  "rate_name" varchar(100) NOT NULL,
  "rate_type" rate_type NOT NULL,
  "property_type" kpr_property_type NOT NULL,
  "customer_segment" customer_segment NOT NULL,
  "base_rate" decimal(5,4) NOT NULL,
  "margin" decimal(5,4) NOT NULL,
  "effective_rate" decimal(5,4) NOT NULL,
  "min_loan_amount" decimal(15,2) NOT NULL,
  "max_loan_amount" decimal(15,2) NOT NULL,
  "min_term_years" integer NOT NULL,
  "max_term_years" integer NOT NULL,
  "max_ltv_ratio" decimal(5,4) NOT NULL,
  "min_income" decimal(15,2) NOT NULL,
  "max_age" integer NOT NULL,
  "min_down_payment_percent" decimal(5,2) NOT NULL,
  "admin_fee" decimal(15,2) DEFAULT 0,
  "admin_fee_percent" decimal(5,4) DEFAULT 0,
  "appraisal_fee" decimal(15,2) DEFAULT 0,
  "insurance_rate" decimal(5,4) DEFAULT 0,
  "notary_fee_percent" decimal(5,4) DEFAULT 0,
  "is_promotional" boolean DEFAULT false,
  "promo_description" text,
  "promo_start_date" date,
  "promo_end_date" date,
  "is_active" boolean DEFAULT true,
  "effective_date" date NOT NULL,
  "expiry_date" date,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "created_by" integer NOT NULL
);

CREATE TABLE "rate_history" (
  "id" SERIAL PRIMARY KEY,
  "kpr_rate_id" integer NOT NULL,
  "old_effective_rate" decimal(5,4) NOT NULL,
  "new_effective_rate" decimal(5,4) NOT NULL,
  "change_reason" text NOT NULL,
  "effective_date" date NOT NULL,
  "changed_by" integer NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "approval_levels" (
  "id" SERIAL PRIMARY KEY,
  "level_name" varchar(100) NOT NULL,
  "level_order" integer NOT NULL,
  "role_required" varchar(100) NOT NULL,
  "min_loan_amount" decimal(15,2) DEFAULT 0,
  "max_loan_amount" decimal(15,2),
  "is_required" boolean DEFAULT true,
  "can_skip" boolean DEFAULT false,
  "timeout_hours" integer DEFAULT 72,
  "description" text,
  "is_active" boolean DEFAULT true,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "approval_matrix" (
  "id" SERIAL PRIMARY KEY,
  "loan_amount_min" decimal(15,2) NOT NULL,
  "loan_amount_max" decimal(15,2) NOT NULL,
  "property_type" kpr_property_type NOT NULL,
  "customer_segment" customer_segment NOT NULL,
  "required_approvals" json NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "branch_staff" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer NOT NULL,
  "branch_code" varchar(10) NOT NULL,
  "staff_id" varchar(20) UNIQUE NOT NULL,
  "position" staff_position NOT NULL,
  "supervisor_id" integer,
  "is_active" boolean DEFAULT true,
  "start_date" date NOT NULL,
  "end_date" date,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "kpr_applications" (
  "id" SERIAL PRIMARY KEY,
  "application_number" varchar(20) UNIQUE NOT NULL,
  "user_id" integer NOT NULL,
  "property_id" integer,
  "kpr_rate_id" integer NOT NULL,
  "property_type" application_property_type NOT NULL,
  "property_value" decimal(15,2) NOT NULL,
  "loan_amount" decimal(15,2) NOT NULL,
  "loan_term_years" integer NOT NULL,
  "interest_rate" decimal(5,4) NOT NULL,
  "monthly_installment" decimal(15,2) NOT NULL,
  "down_payment" decimal(15,2) NOT NULL,
  "property_address" text NOT NULL,
  "property_certificate_type" property_certificate_type NOT NULL,
  "developer_name" varchar(100),
  "purpose" application_purpose NOT NULL,
  "status" application_status DEFAULT 'draft',
  "current_approval_level" integer,
  "submitted_at" timestamp,
  "reviewed_at" timestamp,
  "approved_at" timestamp,
  "rejected_at" timestamp,
  "rejection_reason" text,
  "notes" text,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "application_documents" (
  "id" SERIAL PRIMARY KEY,
  "application_id" integer NOT NULL,
  "document_type" document_type NOT NULL,
  "document_name" varchar(255) NOT NULL,
  "file_path" varchar(500) NOT NULL,
  "file_size" integer NOT NULL,
  "mime_type" varchar(100) NOT NULL,
  "is_verified" boolean DEFAULT false,
  "verified_by" integer,
  "verified_at" timestamp,
  "verification_notes" text,
  "uploaded_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "eligibility_checks" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer NOT NULL,
  "monthly_income" decimal(15,2) NOT NULL,
  "monthly_expenses" decimal(15,2) NOT NULL,
  "existing_loans" decimal(15,2) DEFAULT 0,
  "credit_score" integer,
  "debt_to_income_ratio" decimal(5,4) NOT NULL,
  "loan_to_value_ratio" decimal(5,4) NOT NULL,
  "is_eligible" boolean NOT NULL,
  "max_loan_amount" decimal(15,2),
  "recommended_term_years" integer,
  "eligibility_notes" text,
  "checked_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "approval_workflow" (
  "id" SERIAL PRIMARY KEY,
  "application_id" integer NOT NULL,
  "approval_level_id" integer NOT NULL,
  "stage" workflow_stage NOT NULL,
  "assigned_to" integer NOT NULL,
  "status" workflow_status DEFAULT 'pending',
  "priority" priority_level DEFAULT 'normal',
  "due_date" timestamp,
  "started_at" timestamp,
  "completed_at" timestamp,
  "approval_notes" text,
  "rejection_reason" text,
  "escalated_to" integer,
  "escalated_at" timestamp,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "loans" (
  "id" SERIAL PRIMARY KEY,
  "loan_number" varchar(20) UNIQUE NOT NULL,
  "application_id" integer UNIQUE NOT NULL,
  "user_id" integer NOT NULL,
  "principal_amount" decimal(15,2) NOT NULL,
  "interest_rate" decimal(5,4) NOT NULL,
  "term_months" integer NOT NULL,
  "monthly_installment" decimal(15,2) NOT NULL,
  "outstanding_balance" decimal(15,2) NOT NULL,
  "start_date" date NOT NULL,
  "end_date" date NOT NULL,
  "status" loan_status DEFAULT 'active',
  "disbursement_date" date,
  "disbursement_amount" decimal(15,2),
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "payment_schedules" (
  "id" SERIAL PRIMARY KEY,
  "loan_id" integer NOT NULL,
  "installment_number" integer NOT NULL,
  "due_date" date NOT NULL,
  "principal_amount" decimal(15,2) NOT NULL,
  "interest_amount" decimal(15,2) NOT NULL,
  "total_amount" decimal(15,2) NOT NULL,
  "outstanding_balance" decimal(15,2) NOT NULL,
  "status" payment_status DEFAULT 'pending',
  "paid_date" date,
  "paid_amount" decimal(15,2) DEFAULT 0,
  "late_fee" decimal(15,2) DEFAULT 0,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "transactions" (
  "id" SERIAL PRIMARY KEY,
  "transaction_number" varchar(20) UNIQUE NOT NULL,
  "loan_id" integer NOT NULL,
  "payment_schedule_id" integer,
  "transaction_type" transaction_type NOT NULL,
  "amount" decimal(15,2) NOT NULL,
  "payment_method" payment_method NOT NULL,
  "reference_number" varchar(100),
  "status" transaction_status DEFAULT 'pending',
  "processed_at" timestamp,
  "notes" text,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "auto_debits" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer NOT NULL,
  "loan_id" integer NOT NULL,
  "bank_account_id" integer NOT NULL,
  "is_active" boolean DEFAULT true,
  "debit_date" integer NOT NULL,
  "max_retry_attempts" integer DEFAULT 3,
  "current_retry_count" integer DEFAULT 0,
  "last_debit_date" date,
  "last_debit_status" debit_status,
  "next_debit_date" date NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "bank_accounts" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer NOT NULL,
  "bank_name" varchar(100) NOT NULL,
  "account_number" varchar(20) NOT NULL,
  "account_holder_name" varchar(100) NOT NULL,
  "account_type" account_type NOT NULL,
  "is_verified" boolean DEFAULT false,
  "is_primary" boolean DEFAULT false,
  "verification_method" verification_method,
  "verified_at" timestamp,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "debit_attempts" (
  "id" SERIAL PRIMARY KEY,
  "auto_debit_id" integer NOT NULL,
  "payment_schedule_id" integer NOT NULL,
  "attempt_number" integer NOT NULL,
  "amount" decimal(15,2) NOT NULL,
  "status" debit_attempt_status NOT NULL,
  "response_code" varchar(10),
  "response_message" text,
  "attempted_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "cron_jobs" (
  "id" SERIAL PRIMARY KEY,
  "job_name" varchar(100) UNIQUE NOT NULL,
  "job_type" job_type NOT NULL,
  "schedule_expression" varchar(100) NOT NULL,
  "is_active" boolean DEFAULT true,
  "last_run_at" timestamp,
  "next_run_at" timestamp NOT NULL,
  "run_count" integer DEFAULT 0,
  "success_count" integer DEFAULT 0,
  "failure_count" integer DEFAULT 0,
  "max_retries" integer DEFAULT 3,
  "timeout_seconds" integer DEFAULT 300,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "job_executions" (
  "id" SERIAL PRIMARY KEY,
  "cron_job_id" integer NOT NULL,
  "execution_id" varchar(36) UNIQUE NOT NULL,
  "status" execution_status NOT NULL,
  "started_at" timestamp NOT NULL,
  "completed_at" timestamp,
  "duration_seconds" integer,
  "records_processed" integer DEFAULT 0,
  "records_success" integer DEFAULT 0,
  "records_failed" integer DEFAULT 0,
  "error_message" text,
  "execution_log" text
);

CREATE TABLE "system_notifications" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer,
  "notification_type" notification_type NOT NULL,
  "title" varchar(255) NOT NULL,
  "message" text NOT NULL,
  "channel" notification_channel NOT NULL,
  "status" notification_status DEFAULT 'pending',
  "scheduled_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "sent_at" timestamp,
  "delivered_at" timestamp,
  "read_at" timestamp,
  "metadata" json,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "dashboard_metrics" (
  "id" SERIAL PRIMARY KEY,
  "metric_type" metric_type NOT NULL,
  "metric_value" decimal(15,2) NOT NULL,
  "metric_date" date NOT NULL,
  "user_id" integer,
  "additional_data" json,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "audit_logs" (
  "id" SERIAL PRIMARY KEY,
  "user_id" integer,
  "action" varchar(100) NOT NULL,
  "table_name" varchar(100) NOT NULL,
  "record_id" integer,
  "old_values" json,
  "new_values" json,
  "ip_address" varchar(45) NOT NULL,
  "user_agent" text,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "system_settings" (
  "id" SERIAL PRIMARY KEY,
  "setting_key" varchar(100) UNIQUE NOT NULL,
  "setting_value" text NOT NULL,
  "setting_type" setting_type NOT NULL,
  "description" text,
  "is_encrypted" boolean DEFAULT false,
  "updated_by" integer NOT NULL,
  "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "users" IS 'Core user table with enhanced security features';

COMMENT ON TABLE "roles" IS 'Role-based access control (RBAC) for system security';

COMMENT ON TABLE "user_profiles" IS 'Extended user profile with KYC (Know Your Customer) data';

COMMENT ON TABLE "user_sessions" IS 'Session management for security tracking';

COMMENT ON TABLE "properties" IS 'Comprehensive property listings with detailed specifications';

COMMENT ON TABLE "developers" IS 'Property developers and real estate companies';

COMMENT ON TABLE "property_images" IS 'Property image gallery with categorization';

COMMENT ON TABLE "property_features" IS 'Detailed property features and amenities';

COMMENT ON TABLE "property_locations" IS 'Points of interest near properties';

COMMENT ON TABLE "property_favorites" IS 'User favorite properties';

COMMENT ON TABLE "property_inquiries" IS 'Property inquiries from potential buyers';

COMMENT ON TABLE "kpr_rates" IS 'KPR interest rates and loan terms configuration';

COMMENT ON TABLE "rate_history" IS 'Historical changes to KPR rates';

COMMENT ON TABLE "approval_levels" IS 'Configurable approval hierarchy levels';

COMMENT ON TABLE "approval_matrix" IS 'Approval matrix based on loan amount and criteria';

COMMENT ON TABLE "branch_staff" IS 'Branch staff hierarchy and positions';

COMMENT ON TABLE "kpr_applications" IS 'Main KPR application table with property and rate linking';

COMMENT ON TABLE "application_documents" IS 'Document management for KPR applications';

COMMENT ON TABLE "eligibility_checks" IS 'Pre-qualification and eligibility assessment';

COMMENT ON TABLE "approval_workflow" IS 'Enhanced approval workflow with hierarchical levels';

COMMENT ON TABLE "loans" IS 'Active loan records after approval';

COMMENT ON TABLE "payment_schedules" IS 'Monthly payment schedule for each loan';

COMMENT ON TABLE "transactions" IS 'All financial transactions related to loans';

COMMENT ON TABLE "auto_debits" IS 'Auto debit configuration for loan payments';

COMMENT ON TABLE "bank_accounts" IS 'User bank accounts for auto debit';

COMMENT ON TABLE "debit_attempts" IS 'Log of all auto debit attempts';

COMMENT ON TABLE "cron_jobs" IS 'Cron job definitions and scheduling';

COMMENT ON TABLE "job_executions" IS 'Individual cron job execution logs';

COMMENT ON TABLE "system_notifications" IS 'System notifications and alerts';

COMMENT ON TABLE "dashboard_metrics" IS 'Dashboard metrics and KPIs';

COMMENT ON TABLE "audit_logs" IS 'Comprehensive audit trail for compliance';

COMMENT ON TABLE "system_settings" IS 'System configuration settings';

ALTER TABLE "users" ADD FOREIGN KEY ("role_id") REFERENCES "roles" ("id");

ALTER TABLE "users" ADD FOREIGN KEY ("id") REFERENCES "user_profiles" ("user_id");

ALTER TABLE "user_sessions" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "properties" ADD FOREIGN KEY ("developer_id") REFERENCES "developers" ("id");

ALTER TABLE "developers" ADD FOREIGN KEY ("verified_by") REFERENCES "users" ("id");

ALTER TABLE "property_images" ADD FOREIGN KEY ("property_id") REFERENCES "properties" ("id");

ALTER TABLE "property_features" ADD FOREIGN KEY ("property_id") REFERENCES "properties" ("id");

ALTER TABLE "property_locations" ADD FOREIGN KEY ("property_id") REFERENCES "properties" ("id");

ALTER TABLE "property_favorites" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "property_favorites" ADD FOREIGN KEY ("property_id") REFERENCES "properties" ("id");

ALTER TABLE "property_inquiries" ADD FOREIGN KEY ("property_id") REFERENCES "properties" ("id");

ALTER TABLE "property_inquiries" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "property_inquiries" ADD FOREIGN KEY ("assigned_to") REFERENCES "users" ("id");

ALTER TABLE "kpr_rates" ADD FOREIGN KEY ("created_by") REFERENCES "users" ("id");

ALTER TABLE "rate_history" ADD FOREIGN KEY ("kpr_rate_id") REFERENCES "kpr_rates" ("id");

ALTER TABLE "rate_history" ADD FOREIGN KEY ("changed_by") REFERENCES "users" ("id");

-- Remove invalid foreign key constraint on JSON field
-- ALTER TABLE "approval_matrix" ADD FOREIGN KEY ("required_approvals") REFERENCES "approval_levels" ("id");

ALTER TABLE "branch_staff" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "branch_staff" ADD FOREIGN KEY ("supervisor_id") REFERENCES "branch_staff" ("id");

ALTER TABLE "kpr_applications" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "kpr_applications" ADD FOREIGN KEY ("property_id") REFERENCES "properties" ("id");

ALTER TABLE "kpr_applications" ADD FOREIGN KEY ("kpr_rate_id") REFERENCES "kpr_rates" ("id");

ALTER TABLE "kpr_applications" ADD FOREIGN KEY ("current_approval_level") REFERENCES "approval_levels" ("id");

ALTER TABLE "application_documents" ADD FOREIGN KEY ("application_id") REFERENCES "kpr_applications" ("id");

ALTER TABLE "application_documents" ADD FOREIGN KEY ("verified_by") REFERENCES "users" ("id");

ALTER TABLE "eligibility_checks" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "approval_workflow" ADD FOREIGN KEY ("application_id") REFERENCES "kpr_applications" ("id");

ALTER TABLE "approval_workflow" ADD FOREIGN KEY ("approval_level_id") REFERENCES "approval_levels" ("id");

ALTER TABLE "approval_workflow" ADD FOREIGN KEY ("assigned_to") REFERENCES "users" ("id");

ALTER TABLE "approval_workflow" ADD FOREIGN KEY ("escalated_to") REFERENCES "users" ("id");

ALTER TABLE "kpr_applications" ADD FOREIGN KEY ("id") REFERENCES "loans" ("application_id");

ALTER TABLE "loans" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "payment_schedules" ADD FOREIGN KEY ("loan_id") REFERENCES "loans" ("id");

ALTER TABLE "transactions" ADD FOREIGN KEY ("loan_id") REFERENCES "loans" ("id");

ALTER TABLE "transactions" ADD FOREIGN KEY ("payment_schedule_id") REFERENCES "payment_schedules" ("id");

ALTER TABLE "auto_debits" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "auto_debits" ADD FOREIGN KEY ("loan_id") REFERENCES "loans" ("id");

ALTER TABLE "auto_debits" ADD FOREIGN KEY ("bank_account_id") REFERENCES "bank_accounts" ("id");

ALTER TABLE "bank_accounts" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "debit_attempts" ADD FOREIGN KEY ("auto_debit_id") REFERENCES "auto_debits" ("id");

ALTER TABLE "debit_attempts" ADD FOREIGN KEY ("payment_schedule_id") REFERENCES "payment_schedules" ("id");

ALTER TABLE "job_executions" ADD FOREIGN KEY ("cron_job_id") REFERENCES "cron_jobs" ("id");

ALTER TABLE "system_notifications" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "dashboard_metrics" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "audit_logs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "system_settings" ADD FOREIGN KEY ("updated_by") REFERENCES "users" ("id");
