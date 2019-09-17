import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.*;  
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler.ChartTheme;

//MEIOUSaveGameReader V 1.01
//CHANGELOG:
//V 0.1: Set up basic systems, hack with XChart (9/8/19)
//V 0.15: Added option to include subjects, heretics are now sorted under their own categories. TODO: Economy (9/9/19)
public class MEIOUSaveGameReaderMain {
	Pattern p = Pattern.compile("\t{1}[A-Z]{3,4}={");
	public static String fileName = "";
	static Charset charset = Charset.forName("ISO-8859-1");
	static HashMap<String, Color> colors = new HashMap<String, Color>();
	static HashMap<String, Double> cultures = new HashMap<String, Double>();
	static HashMap<String, Double> cities = new HashMap<String, Double>();
	static HashMap<String, Double> producedGoods = new HashMap<String, Double>();
	static HashMap<String, Boolean> nations = new HashMap<String, Boolean>();
	static HashMap<String, HashMap<String, Double>> religions = new HashMap<String, HashMap<String, Double>>();

	//World info
	static HashMap<String, HashMap<String, HashMap<String, Double>>> worldReligions = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
	static HashMap<String, HashMap<String, Double>> worldCultures = new HashMap<String, HashMap<String, Double>>();
	static HashMap<String, HashMap<String, Double>> worldCities = new HashMap<String, HashMap<String, Double>>();
	static HashMap<String, HashMap<String, Double>> worldGoods = new HashMap<String, HashMap<String, Double>>();
	static HashMap<String, Double> worldReligionTotal = new HashMap<String, Double>();
	static HashMap<String, String> provinces= new HashMap<String, String>();
	
	//National Rankigns
	static HashMap<String, Double> worldPopulations = new HashMap<String, Double>();
	static HashMap<String, Double> worldEconomies = new HashMap<String, Double>();
	static HashMap<String, Double> worldCityRankings = new HashMap<String, Double>();
	static HashMap<String, Integer> locations = new HashMap<String, Integer>();
	static List<String> lines;
	static List<String> nationNames;
	static HashMap<String, String> nationNamesHM = new HashMap<String, String>();
	static int StaticStartingIndex = 0;
	static DecimalFormat df2 = new DecimalFormat("#.##");
	static String includeWorld = "";
	static String cwd = new File("").getAbsolutePath();
	public static void main(String[] args) throws IOException {
		System.out.println("loading names...");
		loadNationNames();
		Scanner inputScan = new Scanner(System.in);

		//while (1==1) { 
			System.out.println("Enter save game name (include the .eu4):");
			String saveName = inputScan.next();
			fileName = cwd + "\\" + saveName;
			buildColors();
			List<String> provinceList = null;

			long startTime = System.currentTimeMillis();
			System.out.println("Would you like to include subjects as part of the parent country (y/n)?");
			String subjects = inputScan.next();
			//System.out.println("Do you want to compare your nation with the rest of the world (y/n)? WARNING: This can take a while.");
			includeWorld = "y";
			try {
				lines = Files.readAllLines(Paths.get(fileName), charset);
				System.out.println("Save game read!");
			    if (includeWorld.equals("y")) {
			    	System.out.println("Running advanced version");
			    	collectWorldProvinces(subjects);
			    }
			    
			    else if (includeWorld.equals("n")) {
			    	System.out.println("Running basic version...");
					System.out.println("Enter a tag:");

					String tag = inputScan.next();
			    	List<String> nationsList = getNationList(subjects, tag, StaticStartingIndex);
					int startLine = -1;
					for (String nation : nationsList) {
						provinceList = getProvinceList(nation);
						String nationalReligion = findNationalReligion(nation);
					    for (String province : provinceList) {
					    	String toLookFor = "-" + province+"={";
							String culture = null;
							String majorityReligion = null;
							String provName = "";
							double population = 0;
							
							startLine = findStartLine(toLookFor);
							provName = findToLookFor(toLookFor, startLine);
							population = findPop(startLine, provName);
							culture = findCulture(startLine, population);
							majorityReligion = findProvMajorityReligion(startLine);
							findReligiousMinorities(startLine, culture, population, nationalReligion, majorityReligion);
								
							}
							
					    }
					//}

				cultures = sortByValue(cultures);
				double totalPop = 0;
				totalPop = getTotalInHashMap(cultures);
				Iterator hmIterator = cultures.entrySet().iterator(); 
				
				hmIterator = cultures.entrySet().iterator(); 
				PieChart chart = new PieChartBuilder().width(1600).height(1200).title("My Pie Chart").theme(ChartTheme.GGPlot2).build();
			    chart.getStyler().setLegendVisible(false);
			    chart.getStyler().setAnnotationType(org.knowm.xchart.style.PieStyler.AnnotationType.LabelAndPercentage);
			    chart.getStyler().setAnnotationDistance(1.4);
			    chart.getStyler().setPlotContentSize(0.7);
			    chart.getStyler().setStartAngleInDegrees(0.1);
			    chart.getStyler().setDrawAllAnnotations(true);
			    System.out.println("***********************************************");
				System.out.println("*                 Cultures                    *");
				System.out.println("***********************************************");
				System.out.println("Total cultures: " + cultures.size());
				double otherPop = 0;
				while (hmIterator.hasNext()) {
					Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
					double pop = (Double) mapElement.getValue();
					String percPop = df2.format((pop/totalPop) * 100);
					System.out.println(mapElement.getKey() + " population: " + df2.format(pop) + "k (" + percPop + "%)");
					if (pop / totalPop < 0.015) {
						otherPop += pop;
					}
					else {
						chart.addSeries((String)mapElement.getKey(), (Double)mapElement.getValue());
					}

				}
				
				chart.addSeries("other", otherPop);
				System.out.println("");
				System.out.println("Total:" + df2.format(totalPop) + "k");
				System.out.println("");
				System.out.println("***********************************************");
				System.out.println("*                 Religions                   *");
				System.out.println("***********************************************");
				religions = sortBigHashMap(religions);
				hmIterator = religions.entrySet().iterator();
				PieChart religionChart = new PieChartBuilder().width(1600).height(1200).title("My Pie Chart").theme(ChartTheme.GGPlot2).build();
				religionChart.getStyler().setLegendVisible(false);
				religionChart.getStyler().setAnnotationType(org.knowm.xchart.style.PieStyler.AnnotationType.LabelAndPercentage);
				religionChart.getStyler().setAnnotationDistance(1.4);
				religionChart.getStyler().setPlotContentSize(0.7);
				religionChart.getStyler().setStartAngleInDegrees(0.1);
				religionChart.getStyler().setDrawAllAnnotations(true);
				double ReligiousOtherPop = 0;
				while(hmIterator.hasNext()) {
					Map.Entry religionElement = (Map.Entry)hmIterator.next();
					HashMap subHM = (HashMap) religionElement.getValue();
					subHM = sortByValue(subHM);
					Iterator subIterator = subHM.entrySet().iterator();
					double totalOfReligion = 0;
					totalOfReligion = getTotalInHashMap(subHM);
					String religionName = (String)religionElement.getKey();
					if (colors.containsKey(religionName)) {
						if (totalOfReligion / totalPop < 0.015) {
							ReligiousOtherPop += totalOfReligion;
						}
						else {
							religionChart.addSeries(religionName, totalOfReligion).setFillColor(colors.get(religionName));
						}

					}
					
					else {
						if (totalOfReligion / totalPop < 0.015) {
							ReligiousOtherPop += totalOfReligion;
						}
						else {
							religionChart.addSeries(religionName, totalOfReligion);
						}
					}
					subIterator = subHM.entrySet().iterator();
					String percString = df2.format((totalOfReligion / totalPop) * 100);
					System.out.println(religionElement.getKey() + ": " + df2.format(totalOfReligion) + "k (" + percString + "% of population)");
					while (subIterator.hasNext()) {
						Map.Entry subElement = (Map.Entry)subIterator.next();
						String culture = (String) subElement.getKey();
						double population = (Double) subElement.getValue();
						String subPercString = df2.format((population / totalOfReligion) * 100);
						System.out.println("\t" + culture + ": " + df2.format(population) + "k (" + subPercString + "%)");
					}
				}
				religionChart.addSeries("other", ReligiousOtherPop);
				System.out.println("***********************************************");
				System.out.println("*                   Cities                    *");
				System.out.println("***********************************************");
				cities = sortByValue(cities);
				Iterator it = cities.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry city = (Map.Entry)it.next();
					String cityName = (String) city.getKey();
					Double cityPop = (Double) city.getValue();
					if (cityPop > 100) {
						System.out.println(cityName + " : " + cityPop + "k");
					}
				}
				
				System.out.println("***********************************************");
				System.out.println("*               Production Info               *");
				System.out.println("***********************************************");
				double totalGoods = getTotalInHashMap(producedGoods);
				producedGoods = sortByValue(producedGoods);
				Iterator goodsIt = producedGoods.entrySet().iterator();
				System.out.println(producedGoods.size());
				while (goodsIt.hasNext()) {
					Map.Entry good = (Map.Entry)goodsIt.next();
					String goodName = (String) good.getKey();
					Double goodVal = (Double) good.getValue();
					double percGoods = (goodVal /totalGoods) * 100;
					String goodString = df2.format(percGoods);
					System.out.println(goodName + ": " + df2.format(goodVal) + " (" + goodString  + "%)");
				}
				
				System.out.println("total revenue from goods produced: " + df2.format(totalGoods));
				new SwingWrapper(religionChart).displayChart();

			    // Save it
			    BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);

			    // or save it in high-res
			    BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI", BitmapFormat.PNG, 300);

			    // Show it
			    new SwingWrapper(chart).displayChart();

			    // Save it
			    BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);

			    // or save it in high-res
			    BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI", BitmapFormat.PNG, 300);
			    long endTime = System.currentTimeMillis();
			    long finalTime = endTime - startTime;
			    System.out.println("Took " + (finalTime / 1000) + " seconds");
			    }
			    
			  //Scanner scanner = new Scanner(file);
			    //now read the file line by line...
				
			} catch (Exception e){
				
			}
		}		    

	//}
	
	public static void collectWorldProvinces(String subjectsOption) {
		int startIndex = getToStartOfNations();
		String currentNation = "";
		findStartLine("\t---={");
		for (int i = startIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			line = line.replace("\t", "");
			if (line.contains("has_set_government_name=yes")) {
				String tag = lines.get(i-1);
				if (tag.contains("was_player")) { //player nations will need to go up further
					tag = lines.get(i-3);
				}
				tag = tag.replace("\t", "");
				tag  = tag.split("=")[0];
	    		locations.put(tag, i);
				currentNation = tag;
				if (!nations.containsKey(currentNation) && !currentNation.equals(("REB")) && !currentNation.contentEquals("AAA")) {
					if ((!checkForSubject(i) || subjectsOption.equals("n"))) {
						List<String> nationsList = getNationList(subjectsOption, tag, i-1);
						cultures = new HashMap<String, Double>();
						cities = new HashMap<String, Double>();
						religions = new HashMap<String, HashMap<String,Double>>();
						producedGoods = new HashMap<String, Double>();
						int index = 0;
						for (int x = 0; x < nationsList.size(); x++) {
							String nation = nationsList.get(x);
							System.out.println("Gathering data on " + nation);
							if (index != 0 && subjectsOption.equals("y")) {
								String toLookFor = "\t" + nation + "={";
								int id = findStartLine(toLookFor);
								List<String> subList = getNationList(subjectsOption, nation, id);
								subList.remove(0);
								nationsList.addAll(subList);
							}
							index++;
							if (nations.containsKey(nation)) {
							}
							
							else {
		    					nations.put(nation, true);
							}
							
							if (!nationNamesHM.containsKey(nation)) {
								findColonyName(nation);
							}

							List<String> provinceList = getProvinceList(nation);
							if (provinceList.size() != 0) {
								String nationalReligion = findNationalReligion(nation);
							    for (String province : provinceList) {
							    	if (provinces.containsKey(province)) {
							    	}
							    	
							    	else {
							    		provinces.put(province, nation);
							    	}
							    	String toLookFor = "-" + province+"={";
									String culture = null;
									String majorityReligion = null;
									String provName = "";
									double population = 0;
									
									int startLine = findStartLine(toLookFor);

									provName = findToLookFor(toLookFor, startLine);
									population = findPop(startLine, provName);
									culture = findCulture(startLine, population);
									majorityReligion = findProvMajorityReligion(startLine);
									findReligiousMinorities(startLine, culture, population, nationalReligion, majorityReligion);

								}

							}
							
							else {
								nations.remove(currentNation);
							}

						}
						
						if (nations.containsKey(currentNation)) {
							double totalPop = getTotalInHashMap(cultures);
							double totalEco = getTotalInHashMap(producedGoods);
							worldPopulations.put(tag, totalPop);
							worldEconomies.put(tag, totalEco);
							worldCultures.put(tag, cultures);
							worldCities.put(tag, cities);
							worldReligions.put(tag, religions);
							worldGoods.put(tag, producedGoods);
						}
					}

				}

			}
		}
		
		System.out.println("\nAll data gathered!");
		
		printWorldInfo();
	}
	
	public static void printWorldInfo() {
		System.out.println("Top ten most populated nations:");
		worldCultures = sortBigHashMap(worldCultures);
		worldPopulations = sortByValue(worldPopulations);
		worldEconomies = sortByValue(worldEconomies);
		double totalPopInWorld = getTotalInHashMap(worldPopulations);
		Iterator worldPopIt = worldPopulations.entrySet().iterator();
		int size = worldPopulations.size();
		while (worldPopIt.hasNext()) {
			Map.Entry popElement = (Map.Entry) worldPopIt.next();
			String tag = (String) popElement.getKey();
			double totalPop = (Double) popElement.getValue();
			int place = findWorldPlace(worldPopulations, tag);
			double perc = (totalPop / totalPopInWorld) * 100;
			if (place <= 10) {
				System.out.println(nationNamesHM.get(tag) + " (" + tag + "):" + df2.format(totalPop) + "k (" + df2.format(perc) + "% of world population)," + "(#" + place + ")");
			}
		}
		int index = 0;
		//Iterator worldCulturesIt = worldCultures.entrySet().iterator();

		/*while (worldCulturesIt.hasNext()) {

			Map.Entry cultureElement = (Map.Entry) worldCulturesIt.next();
			HashMap<String, Double> subHM = (HashMap<String, Double>) cultureElement.getValue();
			double totalPop = getTotalInHashMap(subHM);
			totalPopInWorld += totalPop;
			if (worldCultures.size() - index <= 10) {
				System.out.println(nationNamesHM.get(cultureElement.getKey()) + " : " + df2.format(totalPop) + "k (#" + (worldCultures.size() - index) + ")");
			}

			index++;
		}*/
		

		System.out.println("Total: " + df2.format(totalPopInWorld) + "k");
		System.out.println("******************************");
		
		System.out.println("Top ten world economies: ");
		double totalOutput = getTotalInHashMap(worldEconomies);
		Iterator worldGoodsIt = worldEconomies.entrySet().iterator();
		while (worldGoodsIt.hasNext()) {

			Map.Entry goodElement = (Map.Entry) worldGoodsIt.next();
			String tag = (String) goodElement.getKey();
			double totalGoods = (Double) goodElement.getValue();
			int place = findWorldPlace(worldEconomies, tag);
			double perc = (totalGoods / totalOutput) * 100;
			if (place <= 10) {
				System.out.println(nationNamesHM.get(tag) + " (" + tag + "):" + df2.format(totalGoods) + " (" + df2.format(perc) + "% of world output), " +  "(#" + place + ")");
			}
		}
		
		System.out.println("******************************");
		System.out.println("Religions of the world:");
		worldReligionTotal = sortByValue(worldReligionTotal);
		Iterator worldReligionIt = worldReligionTotal.entrySet().iterator();
		while (worldReligionIt.hasNext()) {
			Map.Entry religionEntry = (Map.Entry)worldReligionIt.next();
			String religion = (String) religionEntry.getKey();
			double val = (Double) religionEntry.getValue();
			double perc = (val / totalPopInWorld) * 100;
			System.out.println(religion + ": " + df2.format(val) + "k (" + df2.format(perc) + "%)");
		}
		
		worldCityRankings = sortByValue(worldCityRankings);
		Scanner scan = new Scanner(System.in);
		System.out.println("Insert a tag to learn more about a specific nation, or type done to quit. ");
		String answer = scan.next();
		while (!answer.equals("done")) {
			try {

				 try {
					new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				getNationalInfo(answer);

				System.out.println("Insert a tag to learn more about a specific nation, or type done to quit. ");
					
				answer = scan.next();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		
	}
	
	public static int getToStartOfNations() {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.equals("countries={")) {
				return i;
			}
		}
		
		return 0;
	}
	public static HashMap<String, Double> sortByValue(HashMap<String, Double> cultures) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Double> > list = 
               new LinkedList<Map.Entry<String, Double> >(cultures.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
            public int compare(Map.Entry<String, Double> o1,  
                               Map.Entry<String, Double> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>(); 
        for (Map.Entry<String, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    }
	
	public static HashMap<String, HashMap<String, Double>> sortBigHashMap(HashMap<String, HashMap<String, Double>> bigHashMap) {
		double curTotal;
		int indexBefore;
		String[] keys =(String[]) bigHashMap.keySet().toArray(new String[bigHashMap.size()]);
		double[] values = new double[bigHashMap.size()];
		for (int i = 0; i < keys.length; i++) {
			values[i] = getTotalInHashMap(bigHashMap.get(keys[i]));
		}
		 for (int i = 1; i < values.length; i++)
	        {
	            double value = values[i];
	            String key = keys[i];
	            int j = i;
	 
	            // Find the index j within the sorted subset arr[0..i-1]
	            // where element arr[i] belongs
	            while (j > 0 && values[j - 1] > value)
	            {
	            	values[j] = values[j - 1];
	            	keys[j] = keys[j-1];
	                j--;
	            }
	 
	            // Note that sub-array arr[j..i-1] is shifted to
	            // the right by one position i.e. arr[j+1..i]
	            values[j] = value;
	            keys[j] = key;
	    }
		HashMap<String, HashMap<String, Double>> temp = new LinkedHashMap<String, HashMap<String, Double>>();
		for (int i = 0; i < keys.length; i++) {
			
			HashMap<String, Double> toAdd = bigHashMap.get(keys[i]);
			toAdd = sortByValue(toAdd);
			temp.put(keys[i], toAdd);
		}
		return temp;
	}
	
	public static HashMap<String, HashMap<String, HashMap<String, Double>>> sortEvenBiggerHashMap(HashMap<String, HashMap<String, HashMap<String, Double>>> bigHashMap) {
		double curTotal;
		int indexBefore;
		String[] keys =(String[]) bigHashMap.keySet().toArray(new String[bigHashMap.size()]);
		double[] values = new double[bigHashMap.size()];
		for (int i = 0; i < keys.length; i++) {
			values[i] = getTotalInBigHashMap(bigHashMap.get(keys[i]));
		}
		 for (int i = 1; i < values.length; i++)
	        {
	            double value = values[i];
	            String key = keys[i];
	            int j = i;
	 
	            // Find the index j within the sorted subset arr[0..i-1]
	            // where element arr[i] belongs
	            while (j > 0 && values[j - 1] > value)
	            {
	            	values[j] = values[j - 1];
	            	keys[j] = keys[j-1];
	                j--;
	            }
	 
	            // Note that sub-array arr[j..i-1] is shifted to
	            // the right by one position i.e. arr[j+1..i]
	            values[j] = value;
	            keys[j] = key;
	    }
		HashMap<String, HashMap<String, HashMap<String, Double>>> temp = new LinkedHashMap<String, HashMap<String, HashMap<String, Double>>>();
		for (int i = 0; i < keys.length; i++) {
			
			HashMap<String, HashMap<String, Double>> toAdd = bigHashMap.get(keys[i]);
			temp.put(keys[i], toAdd);
		}
		return temp;
	}
	
	public static double getTotalInBigHashMap(HashMap<String, HashMap<String, Double>> hm) {
		double total = 0;
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry)it.next();
			HashMap<String, Double> subMap = (HashMap<String, Double>) element.getValue();
			double totalSubHM = getTotalInHashMap(subMap);
			total += totalSubHM;
		}
		
		return total;
	}
	
	
	public static double getTotalInHashMap(HashMap<String, Double> hm) {
		double total = 0;
		Iterator it = hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry element = (Map.Entry)it.next();
			total += (Double)element.getValue();
		}
		return total;
	}
	
	public static List<String> getNationList(String subjectsOption, String tag, int startIndex) {
		List<String> provList = new ArrayList<String>();
	    int lineNum = 0;
	    boolean foundPlayer = false;
	    boolean provinceListSet = false;
	    String toLookFor = "\t" + tag + "={";
	    List<String> toSearch = new ArrayList<String>();
		toSearch.add(tag);
		if (lines.get(startIndex).contains("was_player")) {
			startIndex-= 2;
		}
	    if (subjectsOption.equals("y")) {
	    	boolean foundTag = false;
	    	for (int i = startIndex; i < lines.size(); i++) {
	    		String line = lines.get(i);
	    		if (line.equals(toLookFor) && !foundPlayer) {
		    		foundPlayer = true;

		    	}
	    		
	    		if (foundPlayer) {
	    			if (line.equals("\t\tsubjects={")) {
	    				line = lines.get(i+1);
	    				line = line.replace("\t", "");
	    				String[] temp = line.split(" ");
	    				for (String nation : temp) {
	    					toSearch.add(nation);
	    				}

	    				return toSearch;
	    			}
	    			
	    			
	    			else if (line.contains("score_rank")) {
	    				String[] toReturn = {tag};
	    				return toSearch;
	    				}
	    			}

	    		}
	    	}
	    
	    else {
	    	return toSearch;
	    }
	    return null;
	    }
	
	
	public static List<String> getProvinceList(String tag) {
	   	String toLookFor = "\t" + tag + "={";
	   	List<String> provList = new ArrayList<String>();
	   	boolean provinceListSet = false;
	   	boolean foundPlayer = true;
	   	int start = 0;
	   	if (locations.containsKey(tag)) {
		   	start = locations.get(tag);
	   	}
	   	
	   	else {
	   		start = findStartLine(toLookFor);
	   	}
	   	for (int i = start; i < lines.size(); i++) {
		   	String line = lines.get(i);
		   	/*if (line.equals(toLookFor) && !foundPlayer) {
		    	foundPlayer = true;
		    	System.out.println("PLAYER FOUND!");
		    } 	*/
		   	if (line.contains("owned_provinces={")) {
		   		line = lines.get(i+1);
		       	line = line.replace("\t", "");
		       	String[] provinceList = line.split(" ");
		       	for (String prov : provinceList) {
		       		provList.add(prov);
		       	}	
		       	

		       	break;
		    }
		   	
		   	else if (line.contains("idea_may_cache")) {
		   		break;
		   	}
		}
	    
	   	return provList;
	}
	
	public static String findNationalReligion(String tag) {
		String toLookFor = "\t" + tag + "={";
		boolean nationFound = false;
		for (int i = StaticStartingIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if(line.equals(toLookFor) && !nationFound) {
				nationFound = true;
			}
			
			else if(line.contains("dominant_religion") && nationFound) {
				line = lines.get(i-1);
				line = line.replace("/t", "");
				String[] string = line.split("=");
				return string[1];
			}
		}
		
		return null;
	}
	
	public static String findProvinceName(int startIndex) {
		for (int i = startIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains("name=") && !line.contains("old") && !line.contains("_")) {
				line = line.replace("\t", "");
				line = line.replace("\"", "");
				String[] lineAr = line.split("=");
				return lineAr[1];
			}
		}
		
		return null;
	}
	
	public static double findUrbanPopulation(int startingIndex) {
		
		for (int i = startingIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains("urban_pop_display=")) {
				line = line.replace("\t", "");
				line = line.replace("\"", "");
				String[] lineAr = line.split("=");
				double toReturn = Double.parseDouble(lineAr[1]);
				return toReturn;
			}
		}
		
		return 0;
	}
	
	public static void findProductionInfo(int startingIndex) {
		String good = "";
		boolean goodGot = false;
		double money = 0;
		String start = lines.get(startingIndex);
		for (int i = startingIndex; i < lines.size(); i++) {
			String line = lines.get(i);

			line = line.replace("\t", "");
			
			if (line.contains("urban_goods") && !line.contains("rank") && !goodGot) {
				good = line.split("=")[0];
				good = good.split("_")[2];
				good = good.replace("luxury", "luxury_cloth");
				good = good.replace("mundane", "unskilled/misc");
				good = good.replace("naval", "naval_supplies");
				goodGot = true;
			}
			
			else if (line.contains("production_urban_value_display") && goodGot) {
				money = Double.parseDouble(line.split("=")[1]);
				if (producedGoods.containsKey(good)) {
					producedGoods.put(good, money + producedGoods.get(good));
					return;
				}
				
				else {
					producedGoods.put(good, money);
					return;
				}

			}
			
			else if (line.contains("culture")) {
				return;
			}
		}
	}
	
	public static int findStartLine(String toLookFor) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.equals(toLookFor)) {
				StaticStartingIndex = i;
				return i;
			}
		}
		return 0;
	}
	
	public static String findToLookFor(String toLookFor, int startingIndex) {
		for (int i = startingIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.equals(toLookFor)) {
				String provName = findProvinceName(i);
				findProductionInfo(i);
				return provName;
			}
		}
		return null;

	}
	
	public static double findPop(int startingIndex1, String provName) {
		for (int i = startingIndex1; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains("total_pop_display")) {
				line = line.replace("\t", "");
				line = line.split("=")[1];
				double population = Double.parseDouble(line);
				
				double cityPopulation = findUrbanPopulation(i);
				worldCityRankings.put(provName, cityPopulation);
				cities.put(provName, cityPopulation);
				return population;
			}
		}
		
		return 0;
	}
	
	public static String findCulture(int startingIndex, double population) {
		for (int i = startingIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains("culture=") && !line.contains("original_culture") && !line.contains("tribal")) {
				String[] temp = line.split("=");
				String culture = temp[1];

				if (!cultures.containsKey(culture)) {
					cultures.put(culture, population);
				}
				
				else {
					cultures.put(culture, cultures.get(culture) + population);
				}
				
				return culture;
			}
		}
		
		return null;
	}
	
	public static String findProvMajorityReligion(int startingIndex) {
		for (int i = startingIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if(line.contains("religion=") && lines.get(i+1).contains("original_religion")) {
				String[] temp = line.split("=");
				String majorityReligion = temp[1];
				return majorityReligion;
			}
		}
		return null;
	}
	
	public static void findReligiousMinorities(int startingIndex, String culture, double population, String nationalReligion, String majorityReligion) {
		double religionPerc = 0;
		for (int i = startingIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if(line.contains("_minority") && !line.contains("estate") && !line.contains("supressed") && !line.contains("noble")) {
				line = line.replace("\t", "");
				line = line.replace("\"", "");
				line = line.substring(9);
				String[] lineArr = line.split("_");
				String religion = lineArr[0];
				religion = religion.replace("west", "west_african");
				double number = 0;
				if (line.contains("heretic")) { //for some dumb fucking reason, heretics work a bit differently.
					number = (Double.parseDouble(lineArr[lineArr.length - 1]) / 2) / 10; //this will return perc
					religion = nationalReligion + "_heretic";
				}
				else {
					number = (Double.parseDouble(lineArr[lineArr.length - 2]) / 2) / 10;
				}
				
				religionPerc += number;
				double totalNumber = population * number;
				if (religions.containsKey(religion)) {
					if (religions.get(religion).containsKey(culture)) { //if the daddy hashmap has the religion, AND the sub-hashmap has the culture
						religions.get(religion).put(culture, religions.get(religion).get(culture) + totalNumber);
					}
					
					else { //if the religion is present, but not the culture
						religions.get(religion).put(culture, totalNumber);
					}
				}
				
				else {
					religions.put(religion, new HashMap<String, Double>());
					religions.get(religion).put(culture, totalNumber);
				}
				
				if (includeWorld.equals("y")) {
					if (worldReligionTotal.containsKey(religion)) {
						worldReligionTotal.put(religion, worldReligionTotal.get(religion) + totalNumber);
					}
					
					else {
						worldReligionTotal.put(religion, totalNumber);
					}
				}
			}
			
			else if (line.contains("trade_power")) {
				double percRemaining = 1.0 - religionPerc;
				double totalNumber = population * percRemaining;
				if (religions.containsKey(majorityReligion)) {
					if (religions.get(majorityReligion).containsKey(culture)) { //if the daddy hashmap has the religion, AND the sub-hashmap has the culture
						religions.get(majorityReligion).put(culture, religions.get(majorityReligion).get(culture) + totalNumber);
					}
					
					else { //if the religion is present, but not the culture
						religions.get(majorityReligion).put(culture, totalNumber);
					}
				}
				
				else {
					religions.put(majorityReligion, new HashMap<String, Double>());
					religions.get(majorityReligion).put(culture, totalNumber);
				}
				
				if (includeWorld.equals("y")) {
					if (worldReligionTotal.containsKey(majorityReligion)) {
						worldReligionTotal.put(majorityReligion, worldReligionTotal.get(majorityReligion) + totalNumber);
					}
					
					else {
						worldReligionTotal.put(majorityReligion, totalNumber);
					}
				}
				return;
			}
		}
	}
	
	public static boolean checkForSubject(int startIndex) {
		boolean isSubject = false;
		for (int i = startIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			line = lines.get(i);
			if (line.contains("Years_as_Subject") || line.contains("Years_as_Vassal")) {
				return true;
			}
			
			else if (line.contains("original_capital")) {
				return false;
			}
		}
		
		return false;
	}
	
	public static void findColonyName(String tag) {
		int lineIndex = findStartLine("\t" + tag + "={");
		for (int i = lineIndex; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains("adjective") && !line.contains("changed")) {
				line = lines.get(i-1);
				line = line.replace("\t", "");
				line = line.replace("\"", "");
				String[] lineAr = line.split("=");
				String val = lineAr[1];
				nationNamesHM.put(tag, val);
				return;
			}
			
			else if (line.contains("country_missions")) {
				return;
			}
		}
	}
	
	public static void getNationalInfo(String tag) throws IOException {
		if (!nations.containsKey(tag)) {
			System.out.println("Invalid tag.");
			return;
		}
		HashMap<String, Double> culturesMap = worldCultures.get(tag);
		culturesMap = sortByValue(culturesMap);
		double totalPop = 0;
		totalPop = getTotalInHashMap(culturesMap);
		Iterator hmIterator = culturesMap.entrySet().iterator(); 
		String nation = nationNamesHM.get(tag);
		hmIterator = culturesMap.entrySet().iterator(); 
		PieChart chart = new PieChartBuilder().width(1600).height(1200).title("Cultures of " + nation).theme(ChartTheme.GGPlot2).build();
	    chart.getStyler().setLegendVisible(false);
	    chart.getStyler().setAnnotationType(org.knowm.xchart.style.PieStyler.AnnotationType.LabelAndPercentage);
	    chart.getStyler().setAnnotationDistance(1.4);
	    chart.getStyler().setPlotContentSize(0.7);
	    chart.getStyler().setStartAngleInDegrees(0.1);
	    chart.getStyler().setDrawAllAnnotations(true);
	    System.out.println("***********************************************");
		System.out.println("*                 Cultures                    *");
		System.out.println("***********************************************");
		System.out.println("Total cultures: " + culturesMap.size());
		double otherPop = 0;
		int worldPopRank = findWorldPlace(worldPopulations, tag);
		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
			double pop = (Double) mapElement.getValue();
			String percPop = df2.format((pop/totalPop) * 100);
			System.out.println(mapElement.getKey() + " population: " + df2.format(pop) + "k (" + percPop + "%)");
			if (pop / totalPop < 0.015) {
				otherPop += pop;
			}
			else {
				chart.addSeries((String)mapElement.getKey(), (Double)mapElement.getValue());
			}

		}
		
		chart.addSeries("other", otherPop);
		System.out.println("");
		System.out.println("Total:" + df2.format(totalPop) + "k (#" + worldPopRank + ")");
		System.out.println("");
		
		
		System.out.println("***********************************************");
		System.out.println("*                 Religions                   *");
		System.out.println("***********************************************");
		
		HashMap<String, HashMap<String,Double>> religionMap = worldReligions.get(tag);
		religionMap = sortBigHashMap(religionMap);
		hmIterator = religionMap.entrySet().iterator();
		
		PieChart religionChart = new PieChartBuilder().width(1600).height(1200).title("Religions of " + nation).theme(ChartTheme.GGPlot2).build();
		religionChart.getStyler().setLegendVisible(false);
		religionChart.getStyler().setAnnotationType(org.knowm.xchart.style.PieStyler.AnnotationType.LabelAndPercentage);
		religionChart.getStyler().setAnnotationDistance(1.4);
		religionChart.getStyler().setPlotContentSize(0.7);
		religionChart.getStyler().setStartAngleInDegrees(0.1);
		religionChart.getStyler().setDrawAllAnnotations(true);
		
		double ReligiousOtherPop = 0;
		while(hmIterator.hasNext()) {
			Map.Entry religionElement = (Map.Entry)hmIterator.next();
			HashMap subHM = (HashMap) religionElement.getValue();
			subHM = sortByValue(subHM);
			Iterator subIterator = subHM.entrySet().iterator();
			double totalOfReligion = 0;
			totalOfReligion = getTotalInHashMap(subHM);
			String religionName = (String)religionElement.getKey();
			if (colors.containsKey(religionName)) {
				if (totalOfReligion / totalPop < 0.015) {
					ReligiousOtherPop += totalOfReligion;
				}
				else {
					religionChart.addSeries(religionName, totalOfReligion).setFillColor(colors.get(religionName));
				}

			}
			
			else {
				if (totalOfReligion / totalPop < 0.015) {
					ReligiousOtherPop += totalOfReligion;
				}
				else {
					religionChart.addSeries(religionName, totalOfReligion);
				}
			}
			subIterator = subHM.entrySet().iterator();
			String percString = df2.format((totalOfReligion / totalPop) * 100);
			System.out.println(religionElement.getKey() + ": " + df2.format(totalOfReligion) + "k (" + percString + "% of population)");
			while (subIterator.hasNext()) {
				Map.Entry subElement = (Map.Entry)subIterator.next();
				String culture = (String) subElement.getKey();
				double population = (Double) subElement.getValue();
				String subPercString = df2.format((population / totalOfReligion) * 100);
				System.out.println("\t" + culture + ": " + df2.format(population) + "k (" + subPercString + "%)");
			}
		}
		religionChart.addSeries("other", ReligiousOtherPop);
		System.out.println("***********************************************");
		System.out.println("*                   Cities                    *");
		System.out.println("***********************************************");
		HashMap<String, Double> citiesMap = worldCities.get(tag);
		citiesMap = sortByValue(citiesMap);
		Iterator it = citiesMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry city = (Map.Entry)it.next();
			String cityName = (String) city.getKey();
			Double cityPop = (Double) city.getValue();
			int cityPlaceInNation = findWorldPlace(citiesMap, cityName);
			if (cityPop > 100) {
				System.out.println(cityName + " : " + cityPop + "k (#" + cityPlaceInNation + " in country) (#" + findWorldPlace(worldCityRankings, cityName) +" in world)" );
			}
		}
		
		HashMap<String, Double> productionMap = worldGoods.get(tag);
		System.out.println("***********************************************");
		System.out.println("*               Production Info               *");
		System.out.println("***********************************************");
		double totalGoods = getTotalInHashMap(productionMap);
		productionMap = sortByValue(productionMap);
		Iterator goodsIt = productionMap.entrySet().iterator();
		
		PieChart productionChart = new PieChartBuilder().width(1600).height(1200).title("Produced goods of " + nation).theme(ChartTheme.GGPlot2).build();
		productionChart.getStyler().setLegendVisible(false);
		productionChart.getStyler().setAnnotationType(org.knowm.xchart.style.PieStyler.AnnotationType.LabelAndPercentage);
		productionChart.getStyler().setAnnotationDistance(1.4);
		productionChart.getStyler().setPlotContentSize(0.7);
		productionChart.getStyler().setStartAngleInDegrees(0.1);
		productionChart.getStyler().setDrawAllAnnotations(true);
		
		while (goodsIt.hasNext()) {
			Map.Entry good = (Map.Entry)goodsIt.next();
			String goodName = (String) good.getKey();
			Double goodVal = (Double) good.getValue();
			double percGoods = (goodVal /totalGoods) * 100;
			String goodString = df2.format(percGoods);
			System.out.println(goodName + ": " + df2.format(goodVal) + " (" + goodString  + "%)");
			productionChart.addSeries(goodName, goodVal);
		}
		
		int place = findWorldPlace(worldEconomies, tag);
		System.out.println("total revenue from goods produced: " + df2.format(totalGoods) + " (#" + place + ")");
		new SwingWrapper(religionChart).displayChart();

		new SwingWrapper(productionChart).displayChart();
	    // Save it

	    // Show it
	    new SwingWrapper(chart).displayChart();

	    // Save it
	}
	
	public static void loadNationNames() {
		String filePath = cwd + "\\localisation\\countries_l_english.yml";
		try {
			nationNames = Files.readAllLines(Paths.get(filePath), charset);
			for (int i = 1; i < nationNames.size(); i++) {
				String line = nationNames.get(i);
				if (!line.contains("ADJ")) {
					String[] lineAr = line.split(":");

					String hash = lineAr[0];
					String val = lineAr[1];
					hash = hash.replace(" ", "");
					val = val.replace("1", "");
					val = val.replace("\"", "");

					nationNamesHM.put(hash, val);
				}
			}
			
			System.out.println("localisation sucsessfully loaded!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("localisation not found.");
			System.exit(0);
		}

	}
	
	public static int findWorldPlace(HashMap<String, Double> hm, String tag) {
		Iterator it = hm.entrySet().iterator();
		int index = 0;
		int sizeOfHm = hm.size();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String toCheck = (String) entry.getKey();
			if (toCheck.equals(tag)) {
				return (sizeOfHm-index);
			}
			index++;
		}
		
		return -1;
	}
	public static void buildColors() {
		colors.put("orthodox", new Color(204, 112, 0));
		colors.put("catholic", Color.YELLOW);
		colors.put("sunni", Color.GREEN);
		colors.put("heretic", Color.WHITE);
		colors.put("reformed", Color.PINK);
		colors.put("coptic", Color.GRAY);
		colors.put("jewish", Color.RED);
		colors.put("wahhabi", new Color(0, 128, 0));
		colors.put("shiite", new Color(144, 238, 144));
		colors.put("gnostic", new Color(239, 221, 111));
		colors.put("protestant", Color.BLUE);
		colors.put("chaldean", new Color(255, 175, 77));
		colors.put("hussite", new Color(135, 206, 235));
		colors.put("durze", new Color(0,139,139));
		colors.put("ibadi", new Color(75,83,32));
		colors.put("taoic", new Color(233, 150, 122));
		colors.put("confucianism", new Color(218, 112, 214));
		colors.put("mahayana", new Color(139, 0, 0));
		colors.put("vajrayana", new Color(255, 69, 0));
		colors.put("hinduism", new Color(176, 224, 230));
		colors.put("sikhism", new Color(32, 178, 170));
		colors.put("jain", new Color(175, 238, 238));
		colors.put("jainism", new Color(175, 238, 238));
		colors.put("zoroastrian", new Color(65, 105, 225));
		colors.put("mesoamerican_religion", new Color(114, 114, 65));
		colors.put("inti", new Color(255, 0, 127));
		colors.put("totemism", new Color(127, 114, 114));
		colors.put("nahuatl", new Color(205, 92, 92));
		colors.put("pantheism", new Color(225, 228, 196));
		colors.put("adi_dharam", new Color(0, 51, 102));
		colors.put("shinto", new Color(76, 0, 51));
		colors.put("animism", new Color(95, 158, 160));
		colors.put("shamanism", new Color(76, 153, 76));
		colors.put("polynesian_religion", new Color(46, 139, 87));
		colors.put("tengri", Color.BLUE);
	}
}
