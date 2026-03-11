-- Drop legacy user_name column from orders (user name is stored in consignee)
ALTER TABLE orders
    DROP COLUMN user_name;
