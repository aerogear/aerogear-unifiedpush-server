--
-- Table structure for table `Category`
--

CREATE TABLE Category (
  id SERIAL NOT NULL,
  name varchar(255),
  PRIMARY KEY (id)
);

-- --------------------------------------------------------

--
-- Table structure for table `Installation_Category`
--

CREATE TABLE Installation_Category (
  Installation_id varchar(255) NOT NULL references Installation(id),
  categories_id bigint NOT NULL references Category(id),
  PRIMARY KEY (Installation_id,categories_id)
);

--
-- Migrate data from `Installation_categories` to `Installation_Category`
--

insert into Category (name) select distinct categories from Installation_categories;
insert into Installation_Category SELECT installation_id, id FROM Installation_categories ic join Category c on c.name = ic.categories;

drop table Installation_categories;