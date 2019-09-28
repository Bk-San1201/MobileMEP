package PSO_GA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import Info.*;

public class PSO_Search {

	int n; // so Sensor
	double r;
	int k;
	public ArrayList<Sensor> list;
	ArrayList<Double>[] pos = new ArrayList[Config.numIndi];
	protected ArrayList<Double>[] pBest = new ArrayList[Config.numIndi];
	public ArrayList<Double> gBest = new ArrayList<Double>();
	ArrayList<Double>[] vel = new ArrayList[Config.numIndi];
	double[] fitnessPBest = new double[Config.numIndi];
	double fitnessGBest;
	ArrayList<ArrayList<Point>> tmp = new ArrayList<>();

	public static double calDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
	}

	public void readData(String filename) {
		String input;
		this.list = new ArrayList<Sensor>();
		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader br = new BufferedReader(fileReader);
			input = br.readLine();
			String[] split = input.split(" ");
			this.n = Integer.parseInt(split[0]);
			this.r = Double.parseDouble(split[1]);
			for (int i = 0; i < n; i++) {
				input = br.readLine();
				split = input.split(" ");
				list.add(new Sensor((int) Double.parseDouble(split[0]), (int) Double.parseDouble(split[1]),
						(int) Double.parseDouble(split[2]), (int) Double.parseDouble(split[3]),
						(int) Double.parseDouble(split[4]), (int) Double.parseDouble(split[5]),
						(int) Double.parseDouble(split[6]), (int) Double.parseDouble(split[7]), r));

			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Double> sub2indi(ArrayList<Double> p1, ArrayList<Double> p2) {
		ArrayList<Double> tmp = new ArrayList<Double>();
//		double phi1, phi2;
		for (int i = 0; i < Config.MAX_LEN; i++) {
			tmp.add(p1.get(i) - p2.get(i));
		}
		return tmp;
	}

	public static ArrayList<Double> addIndivsSub(ArrayList<Double> p, ArrayList<Double> sub) {
		ArrayList<Double> tmp = new ArrayList<Double>();
		double phi, x, y;
		Random r = new Random();
		for (int i = 0; i < Config.MAX_LEN; i++) {
			double sum = p.get(i) + sub.get(i);
			if (sum >= Math.PI / 2 || sum <= (-1) * Math.PI / 2)
				tmp.add(p.get(i));
			else
				tmp.add(p.get(i) + sub.get(i));
		}

		return tmp;
	}

	public double calFitness(ArrayList<Double> indi) {
		for (Sensor s : list) {
			s.setDefault();
		}
		double result = 0;
//		Point tmp = new Point(Config.X0, Config.Y0);
		double tmp_x = Config.X0, tmp_y = Config.Y0;
		for (int i = 1; i <= Config.MAX_LEN; i++) {
			tmp_x += Config.DT * Config.VI * Math.cos(indi.get(i - 1));
			tmp_y += Config.DT * Config.VI * Math.sin(indi.get(i - 1));
			if (tmp_x > Config.W)
				break;
			if (tmp_y > Config.H || tmp_y < 0)
				return Double.MAX_VALUE;
			for (Sensor s : list) {
				s.move(Config.DT * Config.VS);
				result += s.MEx(tmp_x, tmp_y) * Config.DT;
//				s.setDefault();
			}
//			for (int it = 0; it < s.; it) {
//				s.get(it).move()
//			}
		}
		// Code for intruder travel to target
		int len = (int)Math.abs(((Config.YN - tmp_y * 1.0) / Config.DS));
		double sign = 1.0;
		if (Config.YN < tmp_y)
			sign = -1.0;
		while (Math.abs(tmp_y - Config.YN) >= 0.2) {
			tmp_y += sign * Config.DS;
			for (Sensor s : list) {
				s.move(Config.DT * Config.VS);
				result += s.MEx(tmp_x, tmp_y) * Config.DT;
//				s.setDefault();
			}
		}

		return result;
	}

	public void init() {
		Random r = new Random();
		for (int i = 0; i < Config.numIndi; i++) {
			pos[i] = Initializer.initGenes();
			pBest[i] = new ArrayList<Double>();
			vel[i] = new ArrayList<Double>();
			for (int j = 0; j < Config.MAX_LEN; j++) {
				vel[i].add(0.0);
			}
			for (int j = pos[i].size(); j < Config.MAX_LEN; j++) {
				pos[i].add(r.nextDouble() * Math.PI - Math.PI / 2);
			}
		}

		for (int i = 0; i < Config.numIndi; i++) {
			copy(pos[i], pBest[i]);
			fitnessPBest[i] = calFitness(pBest[i]);
		}
		gBest = new ArrayList<Double>();
		double tmp = fitnessPBest[0];
		double tmp_res;
		int res = 0;
		for (int i = 1; i < Config.numIndi; i++) {
			tmp_res = fitnessPBest[i];
			if (tmp_res < tmp) {
				res = i;
				tmp = tmp_res;
			}
		}
		copy(pBest[res], gBest);
		fitnessGBest = fitnessPBest[res];

	}

	public void updatePBest() {
		double tmp1, tmp2;
		for (int i = 0; i < Config.numIndi; i++) {
			tmp1 = calFitness(pos[i]);
			tmp2 = fitnessPBest[i];
			if (tmp1 < tmp2) {
				copy(pos[i], pBest[i]);
				fitnessPBest[i] = tmp1;
			}
		}
	}

	public void updateGBest() {
		int res = 0;
		for (int i = 0; i < Config.numIndi; i++) {
			if (fitnessPBest[i] < fitnessGBest) {
				res = i;
				fitnessGBest = fitnessPBest[i];
			}
		}
		copy(pBest[res], gBest);
	}

	public static void copy(ArrayList<Double> beg, ArrayList<Double> end) {
		end.clear();
		for (Double i : beg) {
			end.add(i);
		}
	}

	public static void Crossover(ArrayList<Double> tar, ArrayList<Double> indi_1, ArrayList<Double> indi_2) {
		double tmp;
		tar.clear();
		Random rd = new Random();
		int pivot = rd.nextInt(750);
		for (int i = 0; i < pivot; i++) {
			tar.add(indi_1.get(i));
		}
		for (int i = pivot; i < Config.MAX_LEN; i++) {
			tar.add(indi_2.get(i));
		}
	}

	public static void Mutation(ArrayList<Double> tar, ArrayList<Double> indi) {
		tar.clear();
		Random rd = new Random();
		int pos_one = rd.nextInt(250);
		int pos_two = rd.nextInt(750) + pos_one;
		Stack<Double> tmp = new Stack<Double>();
		for (int i = pos_one; i < pos_two; i++) {
			tmp.push(indi.get(i));
		}
		for (int i = 0; i < pos_one; i++)
			tar.add(indi.get(i));
		for (int i = pos_one; i < pos_two; i++) {
			tar.add(tmp.peek());
			tmp.pop();
		}
		for (int i = pos_two; i < Config.MAX_LEN; i++)
			tar.add(indi.get(i));
	}

	public void runPSO() {
		Random r = new Random();
		for (int it = 0; it < Config.ITERATOR; it++) {
//				PSO for MEP
			for (int i = 0; i < Config.numIndi; i++) {

				ArrayList<Double> tmpVEL_p = sub2indi(pBest[i], pos[i]);
				ArrayList<Double> tmpVEL_g = sub2indi(gBest, pos[i]);
				for (int j = 0; j < Config.MAX_LEN; j++) {
					vel[i].set(j, Config.C * vel[i].get(j) + Config.C1 * r.nextDouble() * tmpVEL_p.get(j)
							+ Config.C2 * r.nextDouble() * tmpVEL_g.get(j));
				}
				pos[i] = addIndivsSub(pos[i], vel[i]);
			}
//			GA for MEP
			updatePBest();
			updateGBest();
			for (int i = 0; i < Config.numIndi / 2; i++) {
				ArrayList<Double> tmp = new ArrayList<Double>();
				Crossover(tmp, pBest[i], pBest[i + Config.numIndi / 2]);
				double tmp_fitness = calFitness(tmp);
				if (tmp_fitness < fitnessPBest[i]) {
					copy(tmp, pBest[i]);
					copy(tmp, pos[i]);
//					System.out.println("Ok");
				} else if (tmp_fitness < fitnessPBest[i + Config.numIndi / 2]) {
					copy(tmp, pBest[i + Config.numIndi / 2]);
					copy(tmp, pos[i + Config.numIndi / 2]);
//					System.out.println("Ok");
				}
			}
			updatePBest();
			updateGBest();
			for (int i = 0; i < Config.numIndi; i++) {
				ArrayList<Double> tmp = new ArrayList<Double>();
				Mutation(tmp, pBest[i]);
				if (calFitness(tmp) < fitnessPBest[i]) {
					copy(tmp, pBest[i]);
					copy(tmp, pos[i]);
				}

			}
			updatePBest();
			updateGBest();
//			for (int i = 0; i < 20; i++)
//				System.out.print(pos[0].get(i) + " ");
//			System.out.println();

//			double ans = calFitness([0]);
			System.out.println("Fitness " + fitnessGBest);

		}
		System.out.println("Fitness " + fitnessGBest);

	}

	public static void main(String[] args) {
		PSO_Search pso = new PSO_Search();
		pso.readData("./Data/25/test16.txt");
		pso.init();
		pso.runPSO();

	}
}