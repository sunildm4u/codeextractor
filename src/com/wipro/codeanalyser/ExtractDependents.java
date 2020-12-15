package com.wipro.codeanalyser;

import java.util.Collection;

import com.jeantessier.classreader.ClassfileLoader;
import com.jeantessier.classreader.LoadListenerVisitorAdapter;
import com.jeantessier.classreader.TransientClassfileLoader;
import com.jeantessier.commandline.CommandLineException;
import com.jeantessier.dependency.CodeDependencyCollector;
import com.jeantessier.dependency.LinkMaximizer;
import com.jeantessier.dependency.LinkMinimizer;
import com.jeantessier.dependency.NodeFactory;
import com.jeantessier.dependency.SelectionCriteria;
import com.jeantessier.dependencyfinder.cli.DirectoryExplorerCommand;

public class ExtractDependents extends DirectoryExplorerCommand{
	
	
	public static void main(String[] args) {
		
		try {
			new ExtractDependents().run(args);
				
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
	}

    protected void populateCommandLineSwitches() {
        super.populateCommandLineSwitches();
        populateCommandLineSwitchesForXMLOutput(com.jeantessier.dependency.XMLPrinter.DEFAULT_ENCODING, com.jeantessier.dependency.XMLPrinter.DEFAULT_DTD_PREFIX, com.jeantessier.dependency.XMLPrinter.DEFAULT_INDENT_TEXT);
        populateCommandLineSwitchesForFiltering();
        getCommandLine().addToggleSwitch("list");
        getCommandLine().addToggleSwitch("out");
        getCommandLine().addToggleSwitch("class-scope");
        
      
        getCommandLine().addToggleSwitch("maximize");
        getCommandLine().addToggleSwitch("minimize");

        getCommandLine().addToggleSwitch("xml");
    }

    protected Collection<CommandLineException> parseCommandLine(String[] args) {
        Collection<CommandLineException> exceptions = super.parseCommandLine(args);

        exceptions.addAll(validateCommandLineForFiltering());

        if (getCommandLine().getToggleSwitch("maximize") && getCommandLine().getToggleSwitch("minimize")) {
            exceptions.add(new CommandLineException("Only one of -maximize or -minimize is allowed"));
        }

        return exceptions;
    }
	
	
	@Override
	protected void doProcessing() throws Exception {

        SelectionCriteria filterCriteria = getFilterCriteria();

        NodeFactory factory = new NodeFactory();
        CodeDependencyCollector collector = new CodeDependencyCollector(factory, filterCriteria);

        ClassfileLoader loader = new TransientClassfileLoader();
        loader.addLoadListener(new LoadListenerVisitorAdapter(collector));
        loader.addLoadListener(getVerboseListener());
        loader.load(getCommandLine().getParameters());

        if (getCommandLine().getToggleSwitch("minimize")) {
            LinkMinimizer minimizer = new LinkMinimizer();
            minimizer.traverseNodes(factory.getPackages().values());
        } else if (getCommandLine().getToggleSwitch("maximize")) {
            LinkMaximizer maximizer = new LinkMaximizer();
            maximizer.traverseNodes(factory.getPackages().values());
        }

        getVerboseListener().print("Printing the graph ...");

        com.jeantessier.dependency.Printer printer;
        if (getCommandLine().getToggleSwitch("xml")) {
            printer = new com.jeantessier.dependency.XMLPrinter(getOut(), getCommandLine().getSingleSwitch("encoding"), getCommandLine().getSingleSwitch("dtd-prefix"));
        } else {
            printer = new com.jeantessier.dependency.TextPrinter(getOut());
        }

        if (getCommandLine().isPresent("indent-text")) {
            printer.setIndentText(getCommandLine().getSingleSwitch("indent-text"));
        }

        printer.traverseNodes(factory.getPackages().values());
    
		
		// TODO Auto-generated method stub
	
	}

}
