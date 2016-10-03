DROP TABLE IF EXISTS `t_versions`;
CREATE TABLE `t_versions` ( `version` VARCHAR(32) NOT NULL PRIMARY KEY ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `t_versions` VALUES ('1.000');

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
	`id` INT NOT NULL PRIMARY KEY,
	`username` VARCHAR(16) NOT NULL UNIQUE,
	`password` VARCHAR(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `t_user` (`id`, `username`, `password`) VALUES(1, 'admin', 'config');

DROP TABLE IF EXISTS `t_player`;
CREATE TABLE `t_player` (
	`id` INT NOT NULL PRIMARY KEY,
	`name` VARCHAR(64) NOT NULL,
	`password` VARCHAR(64) NOT NULL,
	`ip` VARCHAR(24) NULL,
	`create_time` DATETIME NOT NULL,
	`last_time` DATETIME NOT NULL,
	INDEX `id_player_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
