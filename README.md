# microFactory

Software writen and used in my master thesis to generate microservice systems based on model files.

The commandline application takes a JSON model file and generates the required services of the defined microservice architecture based on Quarkus with the corresponding container images and Kubernetes deployments. In addition SVG files visualizing the architecture and an Excel file with metrics is generated.

Examples for model files are stored in resources/models. To build a model the application expects two commandline parameters: the name of the model file and the build command. If no build command is provided only the visualization and metrics are generated. This is usefull to check the model for correctness before triggering a build.

At the moment the application is a prototype that is build and started from an IDE. New model files are expected to be stored in the resource folder. If the application should be bundled as a JAR some additional clean-up and error checking for loading the file and creating folder structures would be necessary.
