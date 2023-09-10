/* eslint-disable react-hooks/exhaustive-deps */
import { createContext, useContext, useEffect, useState } from "react";
import { useParams } from "react-router";
import { useManagementClient } from "../context/Client/ManagementClientContext";

const ProjectDetailContext = createContext(null);
export const useProjectDetail = () => useContext(ProjectDetailContext);

export function ProjectDetailProvider({ children }) {
  const [isLeader, setIsLeader] = useState(false);
  const { id: projectId } = useParams();
  const { getIsLeader } = useManagementClient();

  useEffect(() => {
    const getIsLeaderData = async () => {
      try {
        const isLeaderData = await getIsLeader(projectId);
        setIsLeader((_) => isLeaderData);
      } catch (error) {
        console.log(error);
      }
    };

    getIsLeaderData();

    return () => {
      setIsLeader((_) => false);
    };
  }, [projectId]);

  return (
    <ProjectDetailContext.Provider value={{ isLeader, projectId }}>
      {children}
    </ProjectDetailContext.Provider>
  );
}
