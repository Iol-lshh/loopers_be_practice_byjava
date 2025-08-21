-- docker exec -it $(docker ps -q --filter "ancestor=mysql:8.0") mysql -u root -proot
CREATE DATABASE IF NOT EXISTS paymentgateway;
GRANT ALL PRIVILEGES ON paymentgateway.* TO 'application'@'%';
FLUSH PRIVILEGES;
