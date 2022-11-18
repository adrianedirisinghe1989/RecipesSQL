use recipes;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS step;
DROP TABLE IF EXISTS recipe_category;
DROP TABLE IF EXISTS unit;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS recipe;

CREATE TABLE recipe(
recipe_id INT AUTO_INCREMENT NOT NULL,
recipe_name VARCHAR(128) NOT NULL,
notes TEXT,
num_servings INT,
prep_time TIME,
cook_time TIME,
create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
primary key ( recipe_id)
);

CREATE TABLE category(
category_id INT AUTO_INCREMENT NOT NULL,
category_name VARCHAR(64) NOT null,
primary key(category_id)
);

CREATE TABLE unit(
unit_id INT AUTO_INCREMENT NOT NULL,
unit_name_singular VARCHAR(32) NOT NULL,
unit_name_plural VARCHAR(34) NOT NULL,
primary key(unit_id)
);

CREATE TABLE recipe_category(
recipe_id INT NOT NULL,
category_id INT NOT null,
foreign KEY(recipe_id) references recipe (recipe_id)on delete cascade,
foreign Key(category_id) references  category(category_id) on delete cascade,
unique key (recipe_id,category_id)
);


CREATE TABLE step(
step_id INT AUTO_INCREMENT  NOT NULL,
recipe_id INT NOT NULL,
step_order INT NOT NULL,
step_text TEXT NOT null,
primary key (step_id),
foreign key (recipe_id) references recipe (recipe_id) on delete cascade
);

CREATE TABLE ingredient(
ingredient_id INT auto_increment NOT NULL,
recipe_id INT NOT NULL,
unit_id INT,
ingredient_name VARCHAR(64) NOT NULL,
instruction VARCHAR(64),
ingredient_order INT NOT NULL,
amount DECIMAL(7,2),
primary key (ingredient_id),
foreign kEY( recipe_id) references recipe(recipe_id) on delete cascade,
foreign KEY(unit_id) references unit (unit_id)
);
