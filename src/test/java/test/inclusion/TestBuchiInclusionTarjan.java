package test.inclusion;

import java.util.List;

import automata.IBuchi;
import automata.wa.IBuchiWa;
import main.Options;
import main.TaskInclusion;
import operation.difference.wa.BuchiWaDifferenceTarjan;
import operation.inclusion.wa.BuchiInclusionComplement;
import util.PairXX;
import util.parser.ats.ATSFileParser;

public class TestBuchiInclusionTarjan {
	
	public static void main(String[] args) {
		final String dir = "/home/liyong/projects/liyong/git-repo/ultimate/trunk/examples/"
				+ "automata-benchmarks/20170611-TerminationAnalysis-BuchiIsIncluded/"
				+ "bist_cell_true-unreach-call_false-termination.cil.c_Iteration17.ats";
		ATSFileParser atsParser =  new ATSFileParser();
		atsParser.parse(dir);
		List<PairXX<IBuchiWa>> pairs = atsParser.getBuchiPairs();
		for(PairXX<IBuchiWa> pair : pairs) {
			Options.verbose = true;
			BuchiInclusionComplement complement = new BuchiInclusionComplement(pair.getFstElement(), pair.getSndElement());
			System.out.println("IsIncluded: " + complement.isIncluded() + "");
			//System.out.println(complement.getBuchiDifference().toDot());
			Options.verbose = false;
			BuchiWaDifferenceTarjan difference = new BuchiWaDifferenceTarjan(pair.getFstElement(), pair.getSndElement());
			System.out.println("IsIncluded: " + difference.isIncluded() + "");
		}
		
	}

}
