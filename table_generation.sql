use CEDAH_MEETUP;

CREATE TABLE IF NOT EXISTS logins (
	id int auto_increment PRIMARY KEY,
    username varchar(255),
    password varchar(255)
    ); 

CREATE TABLE IF NOT EXISTS users (
	id int auto_increment PRIMARY KEY,
    name varchar(255),
    email varchar(255),
    phone varchar(255),
    group_id int
    ); 
    
CREATE TABLE IF NOT EXISTS meeting (
	id int auto_increment PRIMARY KEY,
    meeting_day varchar(255),
    meeting_start_time time,
    meeting_end_time time,
    manager_user_id int
    ); 
    
CREATE TABLE IF NOT EXISTS user_availability (
	id int auto_increment PRIMARY KEY,
	day varchar(255),
	start_time time,
	end_time time,
	user_id int
); 

CREATE TABLE IF NOT EXISTS meeting_possibilities (
	id int auto_increment PRIMARY KEY,
    day varchar(255),
    start_time time,
    end_time time,
    vote_count int,
    group_id int
    ); 
    
CREATE TABLE IF NOT EXISTS files (
	id int auto_increment PRIMARY KEY,
    description varchar(255),
    data longblob,
    file_name varchar(255),
    file_size varchar(255),
    file_type varchar(255),
    group_id int
    ); 


