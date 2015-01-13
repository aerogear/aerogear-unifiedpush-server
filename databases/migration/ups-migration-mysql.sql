use unifiedpush;

CREATE TABLE `hibernate_sequence` (
    `next_val` BIGINT(19) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `Category`
--

CREATE TABLE `Category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Installation_Category`
--

CREATE TABLE `Installation_Category` (
  `Installation_id` varchar(255) NOT NULL,
  `categories_id` bigint(20) NOT NULL,
  PRIMARY KEY (`Installation_id`,`categories_id`),
  KEY `FK9A83A563DC2D45CD` (`Installation_id`),
  KEY `FK9A83A563B9183AEF` (`categories_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Migrate data from `Installation_categories` to `Installation_Category`
--

insert into Category (name) select distinct categories from Installation_categories;
insert into Installation_Category SELECT installation_id, id FROM Installation_categories ic join Category c on c.name = ic.categories;

insert into hibernate_sequence (next_val) select max(id) + 1 from Category;

drop table Installation_categories;
