-- Sample supplier
INSERT INTO suppliers (company_name, contact_person, email, phone, status) VALUES
    (
     'MediSupply Ethiopia',
     'Abebe Kebede',
     'abebe@medisupply.et',
     '+251911234567',
     'ACTIVE');

-- Sample drugs
INSERT INTO drugs (name, sku, category, price, stock_qty, expiry_date, supplier_id) VALUES
                                                                                        ('Amoxicillin 500mg', 'AMX-500', 'Antibiotic', 25.00, 200, '2027-01-01', 1),
                                                                                        ('Paracetamol 500mg', 'PCM-500', 'Analgesic', 10.00, 500, '2026-12-01', 1);