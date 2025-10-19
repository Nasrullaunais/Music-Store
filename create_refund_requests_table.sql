-- Create refund_requests table for tracking customer refund applications
CREATE TABLE IF NOT EXISTS refund_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    refund_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_date TIMESTAMP NOT NULL,
    processed_date TIMESTAMP NULL,
    processed_by VARCHAR(255) NULL,
    admin_notes VARCHAR(1000) NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_order_refund (order_id)
);

-- Add index for faster queries
CREATE INDEX idx_refund_status ON refund_requests(status);
CREATE INDEX idx_refund_customer ON refund_requests(customer_id);
CREATE INDEX idx_refund_request_date ON refund_requests(request_date);

