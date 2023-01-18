
class ArgumentParse {
    private static final String rov_C = "--rover-id";
    private static final String portSource_C = "--source-port";
    private static final String portMulti_C = "--multicast-port";
    private static final String IP_Multi_C = "--multicast-ip";
    private static final String IP_Dest_C = "--destination-ip";
    private static final String UDPport_C = "--udp-port";
    private static final String filename_C = "--file-name";
    private static final String verb_C = "--verbose";
    private static final String help_C = "--help";
    private static final String id_rov = "-r";
    private static final String portSource = "-s";
    private static final String portMultiCast = "-m";
    private static final String IP_Multi = "-i";
    private static final String IP_Dest = "-d";
    private static final String UDPport = "-u";
    private static final String filename = "-f";
    private static final String verb = "-v";
    private static final String help = "-h";

    
    void parseArguments(String[] args, Rover rov) throws ExceptArgs {
        boolean missing_filename = true;
        boolean missing_rovID = true;
        boolean missing_UDPport = true;
        
        boolean missing_destIP = true;
        boolean missing_arg = false;
        boolean missing_MultiIP = true;
       
        try {
            for (int i = 0; i < args.length; i++) {
                String argument = args[i];
                if (argument.equals(id_rov) || argument.equals(rov_C)) {
                    rov.rovID = Integer.parseInt(args[i + 1]);
                    missing_rovID = false;
                }
                if (argument.equals(IP_Dest) || argument.equals(IP_Dest_C)) {
                    rov.destIP = args[i + 1];
                    missing_destIP = false;
                }
                if (argument.equals(filename) || argument.equals(filename_C)) {
                    rov.filename = args[i + 1];
                    missing_filename = false;
                }
                if (argument.equals(UDPport) || argument.equals(UDPport_C)) {
                    rov.udpPort = Integer.parseInt(args[i + 1]);
                    missing_UDPport = false;
                }
                if (argument.equals(portSource) || argument.equals(portSource_C)) {
                    rov.RipPort = Integer.parseInt(args[i + 1]);
                }
                if (argument.equals(IP_Multi) || argument.equals(IP_Multi_C)) {
                    rov.MCIP = args[i + 1];
                    missing_MultiIP = false;
                }
                if (argument.equals(verb) || argument.equals(verb_C)) {
                    rov.verbOutputs = false;
                    rov.verbLevel = Integer.parseInt(args[i + 1]);
                }
                if (argument.equals(portMultiCast) || argument.equals(portMulti_C)) {
                    rov.MCPort = Integer.parseInt(args[i + 1]);
                }
                
                if (argument.equals(help) || argument.equals(help_C)) {
                    helpviewoptions();
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptArgs("Incorrect arguments. Please see " + help_C);
        }
        if (missing_rovID) {
            System.err.println(" Rover ID is missing.");
            helpviewoptions();
        }
        if (rov.MCPort == 0) {
            System.out.println(" Assuming Multicast port " + 20001);
            rov.MCPort = 20001;
            missing_arg = true;
        }
        if (rov.RipPort == 0) {
            System.out.println(" Not specified RIP port, using port " + 32768);
            rov.RipPort = 32768;
            missing_arg = true;
        }
        if (missing_MultiIP) {
            System.out.println(" Not specified MultiCast IP, using " +
                    "default IP 233.33.33.33");
            rov.MCIP = "233.33.33.33";
            missing_arg = true;
        }
        if (missing_UDPport) {
            System.out.println(" Not specified UDP Port , using port 6767");
            rov.udpPort = 6767;
            missing_arg = true;
        }
        if ((missing_filename && !missing_destIP) || (missing_destIP && !missing_filename)) {
            System.err.println(" You are missing either the " +
                    "file name or the destination IP address. These two fields are " +
                    "optional, however, must be provided with each other if they are " +
                    "provided at all.");
            helpviewoptions();
        }
        if (missing_arg) {
            System.out.println("See " + help_C + " for options");
        }
    }

   
    private void helpviewoptions() {
        System.out.println();
        String usage = "Usage: java Rover [" + id_rov + " | " + rov_C + "] VALUE | " +
                "[OPTIONAL_FLAG_1 VALUE_1] [OPTIONAL_FLAG_2 VALUE_2] [...]";
        System.out.println(usage);
        System.out.println();

        System.out.println("List of flags");

        System.out.println(id_rov + ": Rover ID: This value should serve as a 1 byte" +
                " identifier to each Rover.");
        System.out.println();

        System.out.println("Optional flags:");
        System.out.println("[" + portSource + " | " + portSource_C + "]: Source " +
                "Port: In RIP, this field is 520. You will need to run the program as root to " +
                "set this value as 520.");
        System.out.println();

        System.out.println("[" + portMultiCast + " | " + portMulti_C + "]: " +
                "multicast port");
        System.out.println();

        System.out.println("[" + IP_Multi + " | " + IP_Multi_C + "]: multicast IP." +
                " The IP where messages are sent to. Defaulted to 233.33.33.33 if not " +
                "specified.");
        System.out.println();

        System.out.println("[" + UDPport + " | " + UDPport_C + "]: the port a Rover " +
                " will listen on for Ripcom packets.");
        System.out.println();

        System.out.println("[" + filename + " | " + filename_C + "]: the file that " +
                "is to be  transmitted. If provided, it MUST exist along with the -d flag.");
        System.out.println();

        System.out.println("[" + IP_Dest + " | " + IP_Dest_C + "]: the " +
                "IP address of the destination Rover must be in the form \"10.0.<rover_id>.0\"");
        System.out.println();

        System.out.println("[" + verb + " | " + verb_C + "]: verbose mode " +
                "<LEVEL>:0: Print all routing tables, all received" +
                " tables whenever received, and all Ripcom packets that are " +
                "received and should be forwarded.\n " +
                "1: Print only Ripcom messages that are received and " +
                "should be forwarded.\n " +
                "2: Default option. Print a Ripcom message only when it reaches " +
                "the  destination address.");
        System.out.println();

        System.exit(1);
    }
}
