package Recipes.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.a.DebugBufferingPacketReader;

import Recipes.entity.Category;
import Recipes.entity.Ingredient;
import Recipes.entity.Recipe;
import Recipes.entity.Step;
import Recipes.entity.Unit;
import Recipes.exception.DbException;
import provided.util.DaoBase;

public class RecipeDao extends DaoBase {
private static final String CATEGORY_TABLE="category";
private static final String INGREDIENT_TABLE= "ingredient";
private static final String RECIPE_TABLE = "recipe";
//private static final String RECIPE_CATEGORY = "recipe_category";
private static final String STEP_TABLE = "step";
private static final String UNIT_TABLE = "unit";
private static final String RECIPE_CATEGORY_TABLE = "recipe_category";

public Optional<Recipe> fetchRecipeById(Integer recipeId){
	String sql= "SELECT * FROM " + RECIPE_TABLE +" WHERE recipe_id = ?";
//	System.out.println(sql);
	
	try(Connection conn= Dbconnection.getConnection()){
		startTransaction(conn);
	try {
		Recipe recipe = null;
		try(PreparedStatement stmt= conn.prepareStatement(sql)){
			setParameter(stmt, 1, recipeId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				if(rs.next()) {
					recipe= extract(rs, Recipe.class);
				}
			}
		}
		if( Objects.nonNull(recipe)) {
			recipe.getIngredients()
			.addAll(fetchRecipeIngredients(conn,recipeId));
			
			recipe.getSteps().addAll(fetchRecipeSteps(conn,recipeId));
			recipe.getCategories().addAll(fetchRecipeCategories(conn,recipeId));
		}
		return Optional.ofNullable(recipe);
	}
	catch(Exception e) {
		rollbackTransaction(conn);
		throw new DbException();
	}
}
 catch (SQLException e) {
	throw new DbException(e);
}

}

private List<Category> fetchRecipeCategories(Connection conn,
		Integer recipeId) throws SQLException {
  //@formatter: off
	String sql = ""
			+ "SELECT c.category_id, c.category_name "
			+ "FROM " + RECIPE_CATEGORY_TABLE + " rc "
			+ "JOIN " + CATEGORY_TABLE + " c  USING (category_id)"
			+ "WHERE rc.recipe_id = ?";
			
	//@formatter:on
	//	System.out.println(sql);
			try(PreparedStatement stmt= conn.prepareStatement(sql)){
				setParameter(stmt, 1, recipeId, Integer.class);
				
				try(ResultSet rs = stmt. executeQuery()){
					List<Category> categories = new LinkedList<Category>();
					
					while(rs.next()) {
						categories.add(extract(rs, Category.class));	
					}
					return categories;
				}
			}
}
private List<Step> fetchRecipeSteps(Connection conn, Integer recipeId) throws SQLException {
	String sql= " SELECT * FROM " + STEP_TABLE+ " s WHERE s. recipe_id = ?";
	
	try(PreparedStatement stmt= conn.prepareStatement(sql)){
		setParameter(stmt, 1, recipeId, Integer.class);
		try(ResultSet rs= stmt.executeQuery()) {
			List<Step> steps = new LinkedList<Step>();		
			while(rs.next()) {
				steps.add(extract(rs, Step.class));
			}	
			return steps;
		}
	}
}

private List<Ingredient> fetchRecipeIngredients(Connection conn, 
		Integer recipeId) throws SQLException {
	// @formatter: off
	String sql= ""
			+ "SELECT i.*, u.unit_name_singular,u.unit_name_plural "
			+ "FROM "+ INGREDIENT_TABLE + " i "
			+ "LEFT JOIN "+ UNIT_TABLE + " u USING (unit_id) "
			+ "WHERE recipe_id = ? "
			+ "ORDER BY i.ingredient_order";
	//@formatter:on
	
	try(PreparedStatement stmt = conn.prepareStatement(sql)){
		setParameter(stmt, 1, recipeId, Integer.class);
		
	 try(ResultSet rs = stmt.executeQuery()){
		 List<Ingredient> ingredients = new LinkedList<Ingredient>();
		 
		 while(rs.next()) {
			 Ingredient ingredient= extract(rs, Ingredient.class);
			 Unit unit = extract(rs, Unit.class);
			 
			 ingredient.setUnit(unit);
			 ingredients.add(ingredient);
			 
		 }
		 
		 return ingredients;
	 }
	}	
}

public List<Recipe> fetchAllRecipes() {
String sql = "SELECT * FROM "+ RECIPE_TABLE + " ORDER BY recipe_name";
try(Connection conn = Dbconnection.getConnection()){
	startTransaction(conn);
	
	try(PreparedStatement stmt = conn.prepareStatement(sql)){
		try(ResultSet rs = stmt.executeQuery()){
			List<Recipe> recipes= new LinkedList<>();
			
			while(rs.next()) {
				recipes.add(extract(rs,Recipe.class));
			}
			
			return recipes;
		}
		
	}
	catch(Exception e) {
	rollbackTransaction(conn);
	throw new DbException(e);
	}
} catch (SQLException e) {	
throw new DbException(e);
}
}


public Recipe insertRecipe(Recipe recipe) {
	// @formatter:off
	String sql = ""
	+ "INSERT INTO " + RECIPE_TABLE + " "
	+ "(recipe_name, notes, num_servings, prep_time,cook_time)"
	+ "VALUES "
	+ "(?, ?, ?, ?, ?)";
	//@formatter:on
	
	try(Connection conn = Dbconnection.getConnection()){
		startTransaction(conn);
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, recipe.getRecipeName(), String.class);
			setParameter(stmt, 2, recipe.getNotes(), String.class);
			setParameter(stmt, 3, recipe.getNumServings(), Integer.class);
			setParameter(stmt, 4, recipe.getPrepTime(), LocalTime.class);
			setParameter(stmt, 5, recipe.getCookTime(), LocalTime.class);
			
			stmt.executeUpdate();
			Integer recipeId= getLastInsertId(conn, RECIPE_TABLE);
			commitTransaction(conn);
			recipe.setRecipeID(recipeId);
			return recipe;
		}
		catch (Exception e) {
			rollbackTransaction(conn);
			throw new DbException();
		}
	}catch (SQLException e) {
		throw new DbException(e);
	}
}
	
public void executeBatch(List<String> sqlBatch) {
	try(Connection  conn= Dbconnection.getConnection()){
		startTransaction(conn);
		
		try(Statement stmt=conn.createStatement()){
			for(String sql: sqlBatch) {
				stmt.addBatch(sql);
			}
			stmt.executeBatch();
			commitTransaction(conn);
		}
		catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
	} catch (SQLException e) {
		throw new DbException(e);
		
	}

}

public List<Unit> fetchAllunits() {
	String sql = " SELECT * FROM" + UNIT_TABLE+ " ORDER By unit_name_singular";
	
	try(Connection conn = Dbconnection.getConnection()){
		startTransaction(conn);
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			try(ResultSet  rs= stmt. executeQuery()){
				List<Unit> units = new LinkedList<>();
				
				while(rs.next()) {
					units.add(extract(rs,Unit.class));
				}
				
				return units;
			}
			
		}
		catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
		
	} catch (SQLException e) {
	throw new DbException(e);
	}
}

public void addIngredientToRecipe(Ingredient ingredient) {
	String sql =" INSERT INTO"+ INGREDIENT_TABLE
			+ "(recipe_id,unit_id, ingredient_name,instructions,ingrdient_order, amount) "
			+"VALUES(?, ?, ?, ?, ?, ?)";
	
	try(Connection conn= Dbconnection.getConnection()){
		startTransaction(conn);
		
		try {
			Integer order = getNextSequenceNumber(conn, ingredient.getRecipeId(), INGREDIENT_TABLE, "recipe_id");
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, ingredient.getRecipeId(), Integer.class);
				setParameter(stmt, 2, ingredient.getUnit().getUnitId(), Integer.class);
				setParameter(stmt, 3, ingredient.getIngredientName(), String.class);
				setParameter(stmt, 4, ingredient.getInstruction(), String.class);
				setParameter(stmt, 5, order, Integer.class);
				setParameter(stmt, 6, ingredient.getAmount(), BigDecimal.class);
				
				stmt.executeUpdate();
				commitTransaction(conn);
			}
			
		}
		catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
	} catch (SQLException e) {
		throw new DbException(e);
	}	
}

public void addStepToRecipe(Step step) {
	String sql = " INSERT INTO"+ STEP_TABLE +"(recipe_id,step_order,step_text)"
		+" VALUES(?, ?, ?)";
	
	try(Connection conn= Dbconnection.getConnection()){
		startTransaction(conn);
		
		Integer order = getNextSequenceNumber(conn, step.getRecipeId(), STEP_TABLE, "recipe_id");
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, step.getRecipeId(), Integer.class);
			setParameter(stmt, 2, order, Integer.class);
			setParameter(stmt, 3, step.getStepText(), String.class);
			
			stmt.executeUpdate();
			commitTransaction(conn);		
		}
		catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
		
	}catch(SQLException e) {
		throw new DbException(e);
	}
	
	
}

public List<Category> fetchAllCategories() {
	String sql= "SELECT * FROM"+ CATEGORY_TABLE+ "ORDER BY category_name";
	
	try(Connection conn = Dbconnection.getConnection()){
	
		try(PreparedStatement stmt= conn.prepareStatement(sql)){
			try(ResultSet rs = stmt.executeQuery()){
				List<Category> categories = new LinkedList<>();
				
				while(rs.next())
					categories.add(extract(rs, Category.class));
				
				return categories;
			}
			
		}	
	catch (SQLException e) {
	rollbackTransaction(conn);
	throw new DbException(e);
	}	
} catch (SQLException e) {
	throw new DbException(e);
}
}

public void addCategoryToRecipe(Integer recipeID, String category) {
	String subQuery = "(SELECT category_id FROM" + CATEGORY_TABLE + "WHERE category_name = ? )";
	
	String sql= "INSERT INTO"+ RECIPE_CATEGORY_TABLE + " (recipe_id, category_id) VALUES(?,"+subQuery+ ")";
	
	try(Connection conn = Dbconnection.getConnection()){
		startTransaction(conn);
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, recipeID, Integer.class);
			setParameter(stmt, 2, category, String.class);
			
			stmt.executeUpdate();
			commitTransaction(conn);
			
		}
		catch(Exception e) {
			throw new DbException(e);
		}
	}
	
	catch (SQLException e) {
		throw new DbException(e);
	}
}

public List<Step> fetchRecipeSteps(Integer recipeId) {
	try(Connection conn = Dbconnection.getConnection()) {
		startTransaction(conn);
			
		try {
			List<Step> steps = fetchRecipeSteps(conn,recipeId);
			commitTransaction(conn);
			
			return steps;
		}
		
	
	catch (Exception e) {
		rollbackTransaction(conn);
		throw new DbException(e);
	
	}
		}
	
	catch(SQLException e) {
	throw new DbException(e);

	}
		}
	}

//public boolean modifyRecipeStep(Step step) {
//String sql ="UPDATE " + STEP_TABLE + " SET step text =?  WHERE step_id = ?";
//
//try(Connection conn= Dbconnection.getConnection()){
//	startTransaction(conn);
//	try(PreparedStatement stmt = conn.prepareStatement(sql)){
//		setParameter(stmt, 1, step.getStepText(), getClass());
//	}
//	catch(Exception e) {
//		rollbackTransaction(conn);
//	}
//} catch (SQLException e) {
//	throw new DbException(e);
////}
//}
//}