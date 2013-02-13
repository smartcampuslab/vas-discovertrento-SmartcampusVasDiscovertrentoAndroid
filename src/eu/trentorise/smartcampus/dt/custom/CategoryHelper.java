/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.dt.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.smartcampus.dt.R;

public class CategoryHelper {

	private static final String POI_NONCATEGORIZED = "Other place";

	private static final String EVENT_NONCATEGORIZED = "Other event";
	
	private static final String STORY_NONCATEGORIZED = "Other story";


	public static CategoryDescriptor[] EVENT_CATEGORIES = new CategoryDescriptor[]{
		new CategoryDescriptor(R.drawable.marker_event_concert, R.drawable.ic_event_concerts, "Concerts", "Concerts"),
		new CategoryDescriptor(R.drawable.marker_event_happy, R.drawable.ic_event_happy, "Happy hours", "Happy hours"),
		new CategoryDescriptor(R.drawable.marker_event_movie, R.drawable.ic_event_movies, "Movies", "Movies"),
		new CategoryDescriptor(R.drawable.marker_event_party, R.drawable.ic_event_parties, "Parties", "Parties"),
		new CategoryDescriptor(R.drawable.marker_event_seminar, R.drawable.ic_event_seminars, "Seminars", "Seminars"),
		new CategoryDescriptor(R.drawable.marker_event_theater, R.drawable.ic_event_theaters, "Theaters", "Theaters"),
		new CategoryDescriptor(R.drawable.marker_event_generic, R.drawable.ic_other_event, EVENT_NONCATEGORIZED, EVENT_NONCATEGORIZED),
		new CategoryDescriptor(R.drawable.marker_event_exhibition, R.drawable.ic_event_exhibition, "Exhibitions", "Exhibitions"),
	};
	
	public static CategoryDescriptor[] POI_CATEGORIES = new CategoryDescriptor[]{
		new CategoryDescriptor(R.drawable.marker_poi_museum, R.drawable.ic_museums, "Museums", "Museums"),
		new CategoryDescriptor(R.drawable.marker_poi_mobility, R.drawable.ic_mobility, "Mobility", "Mobility"),
		new CategoryDescriptor(R.drawable.marker_poi_parking, R.drawable.ic_parking, "Parking", "Parking"),
		new CategoryDescriptor(R.drawable.marker_poi_office, R.drawable.ic_offices, "Offices", "Offices"),
		new CategoryDescriptor(R.drawable.marker_poi_theater, R.drawable.ic_event_theaters, "Theater", "Theater"),
		new CategoryDescriptor(R.drawable.marker_poi_university, R.drawable.ic_university, "University", "University"),
		new CategoryDescriptor(R.drawable.marker_poi_accomodation, R.drawable.ic_accomodation, "Accomodation", "Accomodation"),
		new CategoryDescriptor(R.drawable.marker_poi_library, R.drawable.ic_libraries, "Libraries", "Libraries"),
		new CategoryDescriptor(R.drawable.marker_poi_food, R.drawable.ic_food, "Food", "Food"),
		new CategoryDescriptor(R.drawable.marker_poi_drink, R.drawable.ic_event_happy, "Drink", "Drink"),
		new CategoryDescriptor(R.drawable.marker_poi_cinema, R.drawable.ic_event_movies, "Cinemas", "Cinemas"),
		new CategoryDescriptor(R.drawable.marker_poi_generic, R.drawable.ic_other_poi, POI_NONCATEGORIZED, POI_NONCATEGORIZED),
	};
	
	
	public static CategoryDescriptor[] STORY_CATEGORIES = new CategoryDescriptor[]{
		new CategoryDescriptor(R.drawable.marker_story_leisure, R.drawable.ic_story_leisure, "Leisure", "Leisure"),
		new CategoryDescriptor(R.drawable.marker_story_offices_and_services, R.drawable.ic_story_offices_and_services, "Offices and Services", "Offices and Services"),
		new CategoryDescriptor(R.drawable.marker_story_univerisity, R.drawable.ic_story_university, "University", "University"),
		new CategoryDescriptor(R.drawable.marker_story_culture, R.drawable.ic_story_culture, "Culture", "Culture"),
		new CategoryDescriptor(R.drawable.marker_story_generic, R.drawable.ic_other_story, STORY_NONCATEGORIZED, STORY_NONCATEGORIZED),
	};

	private static Map<String, String> categoryMapping = new HashMap<String, String>();

	private static Map<String, CategoryDescriptor> descriptorMap = new LinkedHashMap<String, CategoryHelper.CategoryDescriptor>();
	static {
		for (CategoryDescriptor event: EVENT_CATEGORIES) {
			descriptorMap.put(event.category, event);
		}
		
		for (CategoryDescriptor poi: POI_CATEGORIES) {
			descriptorMap.put(poi.category, poi);
		}
		
		for (CategoryDescriptor story: STORY_CATEGORIES) {
			descriptorMap.put(story.category, story);
		}
		
		for (String s: descriptorMap.keySet()) {
			categoryMapping.put(s, s);
		}
		categoryMapping.put("Dances", "Theaters");
	}
	
	public static String[] getAllCategories(Set<String> set) {
		List<String> result = new ArrayList<String>();
		for (String key : categoryMapping.keySet()) {
			if (set.contains(categoryMapping.get(key))) {
				if (key.equals(EVENT_NONCATEGORIZED) || key.equals(POI_NONCATEGORIZED) || key.equals(STORY_NONCATEGORIZED)) {
			
					result.add(null);
				}
				result.add(key);
//				set.remove(categoryMapping.get(key));
			}
		}
		return result.toArray(new String[result.size()]);
	}  
	
	public static int getMapIconByType(String type) {
		if (categoryMapping.containsKey(type)) return descriptorMap.get(categoryMapping.get(type)).map_icon;
		return R.drawable.marker_poi_generic;
	}

	
	public static class CategoryDescriptor {
		public int map_icon;
		public int thumbnail;
		public String category;
		public String description;
		
		public CategoryDescriptor(int map_icon, int tumbnail, String category, String descrpition) {
			super();
			this.map_icon = map_icon;
			this.thumbnail = tumbnail;
			this.category = category;
			this.description = descrpition;
		}

		public static String[] getPOICategories() {
			String[] res = new String[POI_CATEGORIES.length];
			for (int i = 0; i < POI_CATEGORIES.length; i++) res[i] = POI_CATEGORIES[i].category;
			return res;
		}
		
		public static String[] getEventCategories(){
			String[] res=new String[EVENT_CATEGORIES.length];
			for(int i=0;i<EVENT_CATEGORIES.length;i++)res[i]=EVENT_CATEGORIES[i].category;
			return res;
		}
		public static String[] getStoryCategories(){
			String[] res=new String[STORY_CATEGORIES.length];
			for(int i=0;i<STORY_CATEGORIES.length;i++)res[i]=STORY_CATEGORIES[i].category;
			return res;
		}
	}

}
