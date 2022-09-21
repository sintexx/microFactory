# microFactory

Software written and used in my master thesis to generate microservice systems based on model files.

## Generation

The command line application takes a JSON model file and generates the required services of the defined microservice architecture based on Quarkus with the corresponding container images and Kubernetes deployments. In addition, SVG files visualizing the architecture and an Excel file with metrics are generated.

Examples of model files are stored in resources/models. To build a model the application expects two command line parameters: the name of the model file and the build command. If no build command is provided only the visualization and metrics are generated. This is useful to check the model for correctness before triggering a build.

At the moment the application is a prototype that is built and started from an IDE. New model files are expected to be stored in the resource folder. If the application should be bundled as a JAR some additional clean-up and error checking for loading the file and creating folder structures would be necessary.

## Evaluation

Under src/main/python several python scripts are stored that were used to evaluate the system. ```jmeterRun``` and ```jmeterRunItem``` are used to start JMeter runs while collecting pod metrics from the Kubernetes API. After the run is complete and the metric files from the generation step are copied to the appropriate folders, the different ```eval``` scripts can be used to plot and analyze the results of the experiments.
