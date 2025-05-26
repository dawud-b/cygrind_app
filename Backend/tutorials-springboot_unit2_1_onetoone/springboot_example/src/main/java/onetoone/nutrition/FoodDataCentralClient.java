package onetoone.nutrition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FoodDataCentralClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fdc.api.key}")
    private String apiKey;

    @Value("${fdc.api.base-url:https://api.nal.usda.gov/fdc/v1}")
    private String baseUrl;

    public FoodDataCentralClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search for foods by name/keyword
     *
     * @param query The search term
     * @param pageSize Number of results to return (default 25)
     * @return List of search results
     */
    public List<Map<String, Object>> searchFoods(String query, int pageSize) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/foods/search")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", query)
                    .queryParam("pageSize", pageSize)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            List<Map<String, Object>> results = new ArrayList<>();
            if (root.has("foods")) {
                JsonNode foods = root.get("foods");

                for (JsonNode food : foods) {
                    Map<String, Object> foodData = new HashMap<>();
                    foodData.put("fdcId", food.get("fdcId").asText());
                    foodData.put("description", food.get("description").asText());

                    // Add basic nutritional info if available
                    if (food.has("foodNutrients")) {
                        JsonNode nutrients = food.get("foodNutrients");
                        for (JsonNode nutrient : nutrients) {
                            if (nutrient.has("nutrientName") && nutrient.has("value")) {
                                String name = nutrient.get("nutrientName").asText();
                                double value = nutrient.get("value").asDouble();

                                switch (name) {
                                    case "Energy":
                                        foodData.put("calories", value);
                                        break;
                                    case "Protein":
                                        foodData.put("protein", value);
                                        break;
                                    case "Carbohydrate, by difference":
                                        foodData.put("carbohydrates", value);
                                        break;
                                    case "Total lipid (fat)":
                                        foodData.put("fat", value);
                                        break;
                                    case "Fiber, total dietary":
                                        foodData.put("fiber", value);
                                        break;
                                    case "Sugars, total including NLEA":
                                        foodData.put("sugar", value);
                                        break;
                                }
                            }
                        }
                    }

                    results.add(foodData);
                }
            }

            return results;
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Error searching foods: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get detailed information for a specific food by FDC ID
     *
     * @param fdcId The FDC ID of the food
     * @return Detailed food data
     */
    public Map<String, Object> getFoodDetails(String fdcId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/food/" + fdcId)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            Map<String, Object> foodData = new HashMap<>();
            foodData.put("fdcId", fdcId);
            foodData.put("description", root.has("description") ? root.get("description").asText() : "Unknown Food");

            // Default values for nutrients
            foodData.put("calories", 0.0);
            foodData.put("protein", 0.0);
            foodData.put("carbohydrates", 0.0);
            foodData.put("fat", 0.0);
            foodData.put("fiber", 0.0);
            foodData.put("sugar", 0.0);

            // Process food nutrients
            if (root.has("foodNutrients")) {
                for (JsonNode nutrient : root.get("foodNutrients")) {
                    if (nutrient.has("nutrient") && nutrient.has("amount")) {
                        JsonNode nutrientInfo = nutrient.get("nutrient");
                        String name = nutrientInfo.get("name").asText();
                        double value = nutrient.get("amount").asDouble();

                        switch (name) {
                            case "Energy":
                                foodData.put("calories", value);
                                break;
                            case "Protein":
                                foodData.put("protein", value);
                                break;
                            case "Carbohydrate, by difference":
                                foodData.put("carbohydrates", value);
                                break;
                            case "Total lipid (fat)":
                                foodData.put("fat", value);
                                break;
                            case "Fiber, total dietary":
                                foodData.put("fiber", value);
                                break;
                            case "Sugars, total including NLEA":
                                foodData.put("sugar", value);
                                break;
                        }
                    }
                }
            }

            // Serving size information
            if (root.has("servingSize") && root.has("servingSizeUnit")) {
                foodData.put("servingSize", root.get("servingSize").asDouble());
                foodData.put("servingUnit", root.get("servingSizeUnit").asText());
            } else {
                foodData.put("servingSize", 100.0);
                foodData.put("servingUnit", "g");
            }

            return foodData;
        } catch (Exception e) {
            // Log error and return basic map with ID
            System.err.println("Error getting food details: " + e.getMessage());
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("fdcId", fdcId);
            errorData.put("error", "Failed to retrieve food details");
            return errorData;
        }
    }
}
