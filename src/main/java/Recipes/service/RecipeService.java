package Recipes.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import Recipes.dao.RecipeDao;
import Recipes.entity.Category;
import Recipes.entity.Ingredient;
import Recipes.entity.Recipe;
import Recipes.entity.Step;
import Recipes.entity.Unit;
import Recipes.exception.DbException;

public class RecipeService {
private static final String SCHEMA_FILE ="recipe_schema.sql";
private static final String DATA_FILE = "recipe_data.sql";



private RecipeDao recipeDao= new RecipeDao();

public Recipe fetchRecipeById(Integer  recipeId) {
	return recipeDao.fetchRecipeById(recipeId)
			.orElseThrow(()-> new NoSuchElementException
				("Recipe with ID=" + recipeId + "does not exist."));
}

public void createAndPopulateTables() {
	loadFromFile(SCHEMA_FILE); 
	loadFromFile(DATA_FILE);
	
}

private void loadFromFile(String fileName) {
	String content = readFileContent(fileName);
	List<String>sqlStatements= convertContentToSqlStatements(content);
	
	
	
	recipeDao.executeBatch(sqlStatements);
	
	
}

private List<String> convertContentToSqlStatements(String content) {
	content = removeComments(content);
	content= replaceWhitespaceSequenceWithSingleSpace(content);
	
	return extractLinesFromContent(content);
	

}

private List<String> extractLinesFromContent(String content) {
	List<String> lines= new LinkedList<>();

	
	while(!content.isEmpty()) {
		int semicolon = content.indexOf(";");
		
		if(semicolon ==-1) {
			if(!content.isBlank()) {
				lines.add(content);
			}
			content= "";	
		}
		else {
			lines.add(content.substring(0,semicolon).trim());
			content = content.substring(semicolon +1);
		}
	}
	return lines;
}

private String replaceWhitespaceSequenceWithSingleSpace(String content) {
	return content.replaceAll("\\s+", " ");
}

private String removeComments(String content) {
StringBuilder builder = new StringBuilder(content);
int commentPos= 0;

while((commentPos = builder.indexOf("--", commentPos)) != -1) {
	int eolPos= builder.indexOf("\n", commentPos+1);
	
	if(eolPos==-1) {
		builder.replace(commentPos,builder.length(), " ");
	}
	else {
		builder.replace(commentPos, eolPos +1,"");
		
	}
}

return builder.toString();
}

private String readFileContent(String fileName) {
	try {
		Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
		return Files.readString(path);
		
	} catch (Exception e) {
	throw new DbException(e);	
	}		
}

//public static void main(String[]args) {
//	new RecipeService().createAndPopulateTables();
//}

public Recipe addRecipe(Recipe recipe) {
	return recipeDao.insertRecipe(recipe);
}

public List<Recipe> fetchRecipes() {
return recipeDao.fetchAllRecipes();

}

public List<Unit> fechUnits() {
return recipeDao.fetchAllunits();
}

public void addIngredient(Ingredient ingredient) {
 recipeDao.addIngredientToRecipe(ingredient);
	
}

public void addStep(Step step) {
	recipeDao.addStepToRecipe(step);
	
}

public List<Category> fetchCategories() {
	return recipeDao.fetchAllCategories();
}

public void addCategoryToRecipe(Integer recipeID, String category) {
	recipeDao.addCategoryToRecipe(recipeID,category);
	
}

public List<Step> fetchSteps(Integer recipeID) {
	return recipeDao.fetchRecipeSteps(recipeID);
}
}
//public void modifyStep(Step step) {
//	if(!recipeDao.modifyRecipeStep(step)) {
//		throw new DbException(" Step with ID="+ step.getStepId()+ "does not exist" );
//	}
//	
//}
//}